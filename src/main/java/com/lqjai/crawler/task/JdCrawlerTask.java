package com.lqjai.crawler.task;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lqjai.common.utils.IdWorker;
import com.lqjai.crawler.pojo.CrawlerConfig;
import com.lqjai.crawler.pojo.CrawlerLog;
import com.lqjai.crawler.pojo.Sku;
import com.lqjai.crawler.pojo.Spu;
import com.lqjai.crawler.service.*;
import com.lqjai.crawler.util.FileUtil;
import com.lqjai.crawler.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JdCrawlerTask {

    @Autowired
    private HttpUtils httpUtils;
    @Autowired
    private FileUtil fileUtil;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private SkuService skuService;
    @Autowired
    private SpuService productService;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CrawlerConfigService crawlerConfigService;
    @Autowired
    private CrawlerLogService crawlerLogService;
    @Value("${qiniu.file.url}")
    public String ossUrl;
    @Value("${qiniu.file.path}")
    public String ossPath;
    @Value("${qiniu.ossStorage}")
    public Boolean ossStorage;

    // ???????????????
    private ExecutorService executorService;

    // ??????????????????
    @PostConstruct
    private void initThreadPool() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(30, 30, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.DiscardPolicy());
        }
    }

    //?????????????????????????????????????????????????????????????????????
    @Scheduled(fixedDelay = 30*1000)
//    @Scheduled(cron = "0 0/10 * * * ?")
    public void itemtask() throws Exception{
        //
        String ip = InetAddress.getLocalHost().getHostAddress();
        CrawlerConfig crawlerConfig = crawlerConfigService.getOne(Wrappers.<CrawlerConfig>lambdaQuery().eq(CrawlerConfig::getIp, ip));
        if(crawlerConfig.getOpenStatus() == 0){
            log.info("\n###### open status == 0,?????????????????????");
        }else {
            //????????????0?????????????????????1????????????0?????????????????????
            if(crawlerConfig.getPage() > 0){
                log.info("\n###### ip:{},????????????????????????,searchUrl:{}, page:{}",ip, crawlerConfig.getSearchUrl(), crawlerConfig.getPage());
                Integer page = crawlerConfig.getPage();
                /**
                 * storage ????????? FileUtil.getStoragePath()???????????????Windows??????????????????????????????, Linux?????????????????????
                 */
                String msg = crawList(crawlerConfig.getSearchUrl()+page,crawlerConfig.getCookies(), fileUtil.getStoragePath());
                crawlerConfigService.decPage(crawlerConfig.getId());
                //????????????
                CrawlerLog crawlerLog = new CrawlerLog().setType(0).setMsg(msg)
                        .setUrl(crawlerConfig.getSearchUrl()+page).setOperator(ip);
                crawlerLogService.save(crawlerLog);
            } else {
                crawlerConfigService.updateById(crawlerConfig.setOpenStatus(0));
            }
        }
     }

    /**
     * ??????url??????????????????
     * @param url
     * @param cookies
     * @param storage ????????????
     * @return
     */
    public String crawList(String url, String cookies, String storage){
        log.info("\n###### crawList url:{}", url);
        Document doc = null;
        try {
            //?????????????????????
           doc = Jsoup.connect(url).get();
        } catch (Exception e) {
            e.printStackTrace();
            log.info("\n###### Jsoup??????????????????:", e);
        }
        //??????spu
        List<String> skuList = doc.select("#J_goodsList li").eachAttr("data-sku");
        log.info("\n###### spuElements size:{}", skuList.size());
        if(CollectionUtils.isEmpty(skuList)) throw new RuntimeException("????????????");
        List<Future<String>> futureList = new ArrayList<>();
        int spuNum = 0;//???????????????????????????
        int skuNum = 0;//?????????????????????
        for(String id: skuList){
            //??????????????????????????????
            Future<String> result = executorService.submit(new CrawlerList("https://item.jd.com/"+id+".html", cookies, storage));
            futureList.add(result);
        }
        for(Future<String> future : futureList){
            try {
                String msg = future.get();
                if(msg.contains("??????????????????")){
                    spuNum++;
                    skuNum += Integer.parseInt(msg.substring(msg.lastIndexOf(":") + 1));
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.info("\n#####################????????????????????????????????????#########################################################", e);
            }
        }

        log.info("\n#################################################");
        log.info("\n###### ?????????????????????spu total:{},sku total:{}", spuNum, skuNum);
        log.info("\n#################################################");
        String msg = "????????????????????????????????????:"+spuNum+",???????????????:"+skuNum;
        return msg;
 }

    /**
     * ??????url??????????????????
     * @param url
     * @param cookies
     * @param storage ????????????
     * @param overwrite ????????????
     * @return
     */
    @Transactional
    public String crawItem(String url, String cookies, String storage, Boolean overwrite){
        log.info("\n###### crawItem url:{}", url);
        //?????????????????????
        String html = null;
        try {
            html = httpUtils.parseByUrl(cookies, url);
        } catch (Exception e) {
            log.info("\n###### parseById error");;
            throw new RuntimeException(e.getMessage());
        }
        Document document = Jsoup.parse(html);

        List<Sku> items = new ArrayList<>();//???????????????
        //??????spu???id
        Spu product = new Spu();

        //???????????????sku???spu
        String dataSpu = document.select(".preview-info .J-follow").attr("data-id");
        if(StringUtils.isEmpty(dataSpu)) {
            log.info("\n#####################????????????????????????????????????start???#########################################################");
            log.info("\n"+html);
            if(StringUtils.isNotEmpty(storage))
                fileUtil.saveFile(storage + File.separator + "log" ,"??????????????????-"+url.substring(url.lastIndexOf("/")+1), "txt", html);
            log.info("\n#####################????????????????????????????????????end???#########################################################");
            throw new RuntimeException("?????????cookie????????????????????????");
        }
        //??????item???id
        product.setId(dataSpu);

        //???????????????????????????false?????????????????????????????????????????????????????????, true????????????????????????????????????saveOrUpdate()
        if(!overwrite){
            //???????????????????????????
            List<Spu> list = productService.list(Wrappers.<Spu>lambdaQuery().eq(Spu::getId,dataSpu));
            if(CollectionUtils.isNotEmpty(list)) throw new RuntimeException("??????????????????");
        }

        //?????????????????????
        String picUrl = document.select("#spec-img").attr("src");
        if(StringUtils.isEmpty(picUrl)) throw new RuntimeException("??????????????????");
        picUrl = picUrl.replace("/n7/", "/n1/"); //??????????????????
        String imageName = picUrl.substring(picUrl.lastIndexOf("/")+1);
        //???????????????????????????
        if(ossStorage){   //??????oss????????????????????????
            product.setImage(ossUrl + ossPath+imageName);
            product.setImages(ossUrl + ossPath+imageName);
        }else {
            product.setImage(picUrl);
            product.setImages(picUrl);
        }
        Boolean pstatus = httpUtils.downloadNetImage(picUrl, storage, ossStorage);
        product.setPstatus(pstatus == true ? 1: 0);

        //?????????????????????,???????????????????????????,?????????????????????
        Map<String, Object> pmap = queryPrice(cookies, dataSpu);
        String commentTotal = document.select("#comment-count .count").text();
        int price = (Integer) pmap.get("p");
        //????????????title???name
        String name = document.select(".itemInfo-wrap .sku-name").text();
        //?????????????????????????????????
        int commentNum = handbleComment(commentTotal);
        product.setOprice(price).setSprice(price).setCommentNum(commentNum);
        //????????????
        String brandName = document.select("#parameter-brand li").attr("title");
        Integer brandId = brandService.getBrandId(brandName);
        //????????????
        List<String> categories = document.select(".crumb .item a").eachText().stream().distinct().collect(Collectors.toList());
        List<Integer> cids = categoryService.getCids(categories);
        product.setCategory1Id(cids.get(0)).setCategory2Id(cids.get(1)).setCategory3Id(cids.get(2));
        //??????????????????
        String specs = getSpecs(document.select("#choose-attrs .p-choose"));
        product.setBrandId(brandId).setSpecItems(specs);

        Elements attr = document.select("#choose-attr-1 div.item");
        List<Future<Sku>> futureList = new ArrayList<>();
        if(attr.size() != 0){
            for(Element spc : attr){
                //??????????????????????????????
                Future<Sku> result = executorService.submit(new CrawlerItem(spc, cookies, dataSpu, price, brandName, storage, commentNum, cids.get(0), categories.get(0)));
                futureList.add(result);
            }
            //??????????????????????????????
            List<Sku> t = items;
            futureList.forEach(future ->{
                try {
                    Sku sku = future.get();
                    t.add(sku);
                } catch (Exception e) {
                   log.info("\n###### ",e);
                }
            });
        }else{ //?????????????????????sku,????????????????????????
            Map<String, Object> map = getSkuInfo(cookies, dataSpu);
            Sku item = new Sku();
            item.setId(dataSpu).setSpuId(dataSpu).setName(name).setImage(product.getImage()).setImages(product.getImage())
                    .setPaymoney(judge(map.get("p").toString(),price)).setBrandName(brandName)
                    .setPrice(judge(map.get("op").toString(),price))
                    .setSn(idWorker.nextId()+"").setNum(RandomUtil.randomInt(2000)+100);;
            items.add(item);
        }

        /**
         * ???????????????????????????????????????????????????htmlunit??????????????????????????????jsoup??????
         */
        String caption = document.select(".itemInfo-wrap .news").text();
        Elements intro = document.select("#J-detail-content");
        Elements imgs = intro.select("img");
        for(Element img : imgs){
            String src = img.attr("data-lazyload");
            if(ossStorage){ //??????????????????????????????????????????img src?????????????????????
                String picName = src.substring(src.lastIndexOf("/")+1);
                img.attr("data-lazyload",ossUrl + ossPath+picName);
                img.attr("src",ossUrl + ossPath+picName);
                httpUtils.downloadNetImage(src,storage, ossStorage);
            }else {
                img.attr("src",src);
                if(StringUtils.isNotEmpty(storage)) //?????????????????????
                    httpUtils.downloadNetImage(src,storage, ossStorage);
            }
        }
        product.setName(name).setCaption(caption).setIntroduction(intro.html()).setSn(idWorker.nextId()+"");
        log.info("\n###### spu name:{}, caption:{}",name, caption);

        if(CollectionUtils.isEmpty(items)) throw new RuntimeException("????????????????????????????????????????????????");
        //???????????????
        log.info("\n###### ????????? item total:{}", items.size());
        //??????id??????
        items = items.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
                new TreeSet<Sku>(Comparator.comparing(Sku::getId))), ArrayList::new));
        log.info("\n###### ????????? item total:{}", items.size());

        product.setStatus("1");
        productService.saveOrUpdate(product);
        skuService.saveOrUpdateBatch(items);
        log.info("\n#################################################");
        log.info("\n###### ?????????????????????spuId:{},sku total:{}", dataSpu, items.size());
        log.info("\n#################################################");
        String msg = "?????????????????????spuId:"+dataSpu+",???????????????:"+items.size();
        return msg;
    }

    @Async
    public void updateItem(String id, String cookies, String ip){
        String url = "https://item.jd.com/"+id+".html";
        String msg = crawItem(url, cookies, null, true);
        //????????????
        CrawlerLog crawlerLog = new CrawlerLog().setType(3).setMsg(msg).setUrl(url).setOperator(ip);
        crawlerLogService.save(crawlerLog);
    }

    private String getSpecs(Elements elements) {
        Map<String, Object> map = new HashMap<>();
        for(Element element : elements){
            String spec = element.attr("data-type");
            final List<String> items = element.select(".item").eachText();
            map.put(spec,items);
        }
        return JSON.toJSONString(map);
    }

    /**
     * ??????????????????
     * @param ck cookie???
     * @param id
     * @return
     */
    public Map<String, Object> getSkuInfo(String ck,String id){
        log.info("\n###### getSkuInfo id:{}",id);
        Map<String, Object> map = new HashMap<>();

        HttpHeaders headers = new HttpHeaders();//header??????
        List<String> cookies = Arrays.asList(ck.split(";"));
        // header??????
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36");
        // cookie??????
        headers.put(HttpHeaders.COOKIE, cookies);

        HttpEntity<String> httpEntity = new HttpEntity(headers);

        Document document = null;
        try {
            document = Jsoup.parse(httpUtils.parseById(ck, id));
        } catch (Exception e) {
            e.printStackTrace();
            log.info("\n###### ",e);
            throw new RuntimeException("Jsoup ??????????????????");
        }
        String title = document.select(".product-intro .sku-name").text(); //????????????

        //?????????????????????
        String picUrl = document.select("#spec-img").attr("src");

        String priceUrl = "https://p.3.cn/prices/mgets?callback=jQuery2414702&pduid=15282860256122085625433&pdpin=&skuIds=J_"+id;
//        String commentUrl = "https://club.jd.com/comment/productCommentSummaries.action?referenceIds="+id;
        log.info("\n###### priceUrl:{}", priceUrl);
        ResponseEntity<String> priceResult = restTemplate.exchange(priceUrl, HttpMethod.GET, httpEntity, String.class);
//        ResponseEntity<String> commentResult = restTemplate.exchange(commentUrl, HttpMethod.GET, httpEntity, String.class);
        Map<String, Object> price = getPrice(priceResult.getBody());
        map.put("p",price.get("p"));
        map.put("op",price.get("op"));
        map.put("title",title);
        map.put("picUrl",picUrl);
//        map.put("comment",commentResult.getBody());
        return map;
    }

    //http??????????????????
    public Map<String, Object> queryPrice(String ck, String id){
        HttpHeaders headers = new HttpHeaders();//header??????
        List<String> cookies = Arrays.asList(ck.split(";"));
        // header??????
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36");
        // cookie??????
        headers.put(HttpHeaders.COOKIE, cookies);
        HttpEntity<String> httpEntity = new HttpEntity(headers);

        String priceUrl = "https://p.3.cn/prices/mgets?callback=jQuery2414702&pduid=15282860256122085625433&pdpin=&skuIds=J_"+id;
        log.info("\n###### priceUrl:{}", priceUrl);
        ResponseEntity<String> priceResult = restTemplate.exchange(priceUrl, HttpMethod.GET, httpEntity, String.class);
        return getPrice(priceResult.getBody());
    }

    private Map<String, Object> getPrice(String str){
        log.info("\n###### getPrice str:{}", str);
        Map<String, Object> map = new HashMap<>();
        try {
            String substring = str.substring(str.indexOf("[")+1, str.lastIndexOf("]"));
            JSONObject jsonObject = JSON.parseObject(substring);
            map = new HashMap<>();
            map.put("p", jsonObject.getBigDecimal("p").movePointRight(2).intValue());
            map.put("op", jsonObject.getBigDecimal("op").movePointRight(2).intValue());
        } catch (Exception e) {
            log.info("\n###### ???????????????????????????",e.getMessage());
            map.put("p",0);
            map.put("op",0);
        }
        return map;
    }
    
    //????????????
    private int handbleComment(String comment){
        log.info("\n###### handbleComment num:{}", comment);
        if(comment.contains("???+"))
            return new BigDecimal(comment.replace("???+", "")).movePointRight(4).intValue() + RandomUtil.randomInt(100);
        else
            return Integer.parseInt(comment.replace("+","")) + RandomUtil.randomInt(100);
    }

    //?????????????????????????????????????????????????????????????????????0?????????????????????????????????????????????????????????
    private Integer judge(String price, Integer p){
        if("0".equals(price)) return p;
        else return Integer.parseInt(price);
    }


    /**
     * ???????????????
     *
     *@Author Kili
     *@Date 2022/4/22 22:53
     */
    private class CrawlerItem implements Callable<Sku> {
        private Element spec;
        private String cookies;
        private String dataSpu;
        private Integer price;
        private String brandName;
        private String storage;
        private Integer commentTotal;
        private Integer categoryId;
        private String categoryName;

        public CrawlerItem(Element spec, String cookies, String dataSpu, Integer price, String brandName, String storage, Integer commentTotal, Integer categoryId, String categoryName) {
            this.spec = spec;
            this.cookies = cookies;
            this.dataSpu = dataSpu;
            this.price = price;
            this.brandName = brandName;
            this.storage = storage;
            this.commentTotal = commentTotal;
            this.categoryId = categoryId;
            this.categoryName = categoryName;
        }

        @Override
        public Sku call() {
            log.info("\n#####################???????????? CrawlerItem ??????????????????#########################################################");
            Sku item = null;
            try {
                item = new Sku();
                String skuId = spec.attr("data-sku");
                Map<String, Object> map = getSkuInfo(cookies, skuId);
                String imgUrl = map.get("picUrl").toString();
                String picName = imgUrl.substring(imgUrl.lastIndexOf("/")+1);
                //??????????????????????????????
                String dbImg = ossStorage ? ossUrl + ossPath+picName : imgUrl;
                item.setId(skuId).setSpuId(dataSpu).setName(map.get("title").toString())
                        .setImage(dbImg).setImages(dbImg).setPaymoney(judge(map.get("p").toString(),price))
                        .setPrice(judge(map.get("op").toString(), price)).setBrandName(brandName).setCommentNum(commentTotal)
                        .setSn(idWorker.nextId()+"").setNum(RandomUtil.randomInt(2000)+100)
                        .setCategoryId(categoryId).setCategoryName(categoryName);
                Boolean pstatus = httpUtils.downloadNetImage(imgUrl, storage, ossStorage);
                item.setPstatus(pstatus == true ? 1: 0);
            } catch (Exception e) {
                log.info("\n###### CrawlerItem ???????????????{}",e);
            }
           return item;
        }
    }

    /**
     * ???????????????
     *
     *@Author Kili
     *@Date 2022/4/22 22:53
     */
    private class CrawlerList implements Callable<String> {
        private String url;
        private String cookies;
        private String storage;


        public CrawlerList(String url, String cookies, String storage) {
            this.url = url;
            this.cookies = cookies;
            this.storage = storage;
        }

        @Override
        public String call() {
            log.info("\n#####################???????????? CrawlerList ??????????????????#########################################################");
            String msg = null;
            try {
                msg = crawItem(url, cookies, storage, false);
            } catch (Exception e) {
                log.info("\n###### CrawlerItem ???????????????{}",e);
                msg = "error";
            }
            return msg;
        }
    }

    public static void main(String[] args) throws Exception{
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        System.out.println(hostAddress);
    }
}



