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

    // 任务执行器
    private ExecutorService executorService;

    // 初始化线程池
    @PostConstruct
    private void initThreadPool() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(30, 30, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.DiscardPolicy());
        }
    }

    //当下载任务完成后，间隔多长时间进行下一次的任务
    @Scheduled(fixedDelay = 30*1000)
//    @Scheduled(cron = "0 0/10 * * * ?")
    public void itemtask() throws Exception{
        //
        String ip = InetAddress.getLocalHost().getHostAddress();
        CrawlerConfig crawlerConfig = crawlerConfigService.getOne(Wrappers.<CrawlerConfig>lambdaQuery().eq(CrawlerConfig::getIp, ip));
        if(crawlerConfig.getOpenStatus() == 0){
            log.info("\n###### open status == 0,爬虫任务不执行");
        }else {
            //页数大于0，爬取后页数减1，页数为0时关闭爬虫任务
            if(crawlerConfig.getPage() > 0){
                log.info("\n###### ip:{},爬虫任务开始执行,searchUrl:{}, page:{}",ip, crawlerConfig.getSearchUrl(), crawlerConfig.getPage());
                Integer page = crawlerConfig.getPage();
                /**
                 * storage 可调用 FileUtil.getStoragePath()方法获取，Windows系统获取当前用户目录, Linux系统不获取路径
                 */
                String msg = crawList(crawlerConfig.getSearchUrl()+page,crawlerConfig.getCookies(), fileUtil.getStoragePath());
                crawlerConfigService.decPage(crawlerConfig.getId());
                //记录日志
                CrawlerLog crawlerLog = new CrawlerLog().setType(0).setMsg(msg)
                        .setUrl(crawlerConfig.getSearchUrl()+page).setOperator(ip);
                crawlerLogService.save(crawlerLog);
            } else {
                crawlerConfigService.updateById(crawlerConfig.setOpenStatus(0));
            }
        }
     }

    /**
     * 根据url爬取整个页面
     * @param url
     * @param cookies
     * @param storage 存储位置
     * @return
     */
    public String crawList(String url, String cookies, String storage){
        log.info("\n###### crawList url:{}", url);
        Document doc = null;
        try {
            //获取商品列表页
           doc = Jsoup.connect(url).get();
        } catch (Exception e) {
            e.printStackTrace();
            log.info("\n###### Jsoup请求页面出错:", e);
        }
        //获取spu
        List<String> skuList = doc.select("#J_goodsList li").eachAttr("data-sku");
        log.info("\n###### spuElements size:{}", skuList.size());
        if(CollectionUtils.isEmpty(skuList)) throw new RuntimeException("链接失效");
        List<Future<String>> futureList = new ArrayList<>();
        int spuNum = 0;//成功爬取主商品数量
        int skuNum = 0;//爬取子商品数量
        for(String id: skuList){
            //多线程爬取子商品信息
            Future<String> result = executorService.submit(new CrawlerList("https://item.jd.com/"+id+".html", cookies, storage));
            futureList.add(result);
        }
        for(Future<String> future : futureList){
            try {
                String msg = future.get();
                if(msg.contains("爬取数据成功")){
                    spuNum++;
                    skuNum += Integer.parseInt(msg.substring(msg.lastIndexOf(":") + 1));
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.info("\n#####################【多线程爬取主商品异常】#########################################################", e);
            }
        }

        log.info("\n#################################################");
        log.info("\n###### 爬取数据成功：spu total:{},sku total:{}", spuNum, skuNum);
        log.info("\n#################################################");
        String msg = "爬取数据成功：主商品数量:"+spuNum+",子商品数量:"+skuNum;
        return msg;
 }

    /**
     * 根据url爬取整个页面
     * @param url
     * @param cookies
     * @param storage 存储位置
     * @param overwrite 是否覆盖
     * @return
     */
    @Transactional
    public String crawItem(String url, String cookies, String storage, Boolean overwrite){
        log.info("\n###### crawItem url:{}", url);
        //获取商品列表页
        String html = null;
        try {
            html = httpUtils.parseByUrl(cookies, url);
        } catch (Exception e) {
            log.info("\n###### parseById error");;
            throw new RuntimeException(e.getMessage());
        }
        Document document = Jsoup.parse(html);

        List<Sku> items = new ArrayList<>();//子商品集合
        //获取spu的id
        Spu product = new Spu();

        //默认第一个sku为spu
        String dataSpu = document.select(".preview-info .J-follow").attr("data-id");
        if(StringUtils.isEmpty(dataSpu)) {
            log.info("\n#####################【爬取商品出错，打印日志start】#########################################################");
            log.info("\n"+html);
            if(StringUtils.isNotEmpty(storage))
                fileUtil.saveFile(storage + File.separator + "log" ,"页面解析异常-"+url.substring(url.lastIndexOf("/")+1), "txt", html);
            log.info("\n#####################【爬取商品出错，打印日志end】#########################################################");
            throw new RuntimeException("请确认cookie或者链接是否有效");
        }
        //设置item的id
        product.setId(dataSpu);

        //判断是否允许覆盖，false则要查询数据库，有重复的就不走下一步了, true则不查数据库，爬取数据了saveOrUpdate()
        if(!overwrite){
            //查询商品是否被存储
            List<Spu> list = productService.list(Wrappers.<Spu>lambdaQuery().eq(Spu::getId,dataSpu));
            if(CollectionUtils.isNotEmpty(list)) throw new RuntimeException("该商品已存在");
        }

        //设置商品的图片
        String picUrl = document.select("#spec-img").attr("src");
        if(StringUtils.isEmpty(picUrl)) throw new RuntimeException("商品主图为空");
        picUrl = picUrl.replace("/n7/", "/n1/"); //修改图片尺寸
        String imageName = picUrl.substring(picUrl.lastIndexOf("/")+1);
        //设置商品的图片位置
        if(ossStorage){   //根据oss状态设置图片链接
            product.setImage(ossUrl + ossPath+imageName);
            product.setImages(ossUrl + ossPath+imageName);
        }else {
            product.setImage(picUrl);
            product.setImages(picUrl);
        }
        Boolean pstatus = httpUtils.downloadNetImage(picUrl, storage, ossStorage);
        product.setPstatus(pstatus == true ? 1: 0);

        //获取商品的价格,价格和评论都有反爬,得主动请求数据
        Map<String, Object> pmap = queryPrice(cookies, dataSpu);
        String commentTotal = document.select("#comment-count .count").text();
        int price = (Integer) pmap.get("p");
        //设置商品title和name
        String name = document.select(".itemInfo-wrap .sku-name").text();
        //赋值原价，现价和评论数
        int commentNum = handbleComment(commentTotal);
        product.setOprice(price).setSprice(price).setCommentNum(commentNum);
        //爬取品牌
        String brandName = document.select("#parameter-brand li").attr("title");
        Integer brandId = brandService.getBrandId(brandName);
        //爬取分类
        List<String> categories = document.select(".crumb .item a").eachText().stream().distinct().collect(Collectors.toList());
        List<Integer> cids = categoryService.getCids(categories);
        product.setCategory1Id(cids.get(0)).setCategory2Id(cids.get(1)).setCategory3Id(cids.get(2));
        //爬取规格信息
        String specs = getSpecs(document.select("#choose-attrs .p-choose"));
        product.setBrandId(brandId).setSpecItems(specs);

        Elements attr = document.select("#choose-attr-1 div.item");
        List<Future<Sku>> futureList = new ArrayList<>();
        if(attr.size() != 0){
            for(Element spc : attr){
                //多线程爬取子商品信息
                Future<Sku> result = executorService.submit(new CrawlerItem(spc, cookies, dataSpu, price, brandName, storage, commentNum, cids.get(0), categories.get(0)));
                futureList.add(result);
            }
            //收集多线程爬取的结果
            List<Sku> t = items;
            futureList.forEach(future ->{
                try {
                    Sku sku = future.get();
                    t.add(sku);
                } catch (Exception e) {
                   log.info("\n###### ",e);
                }
            });
        }else{ //没有规格，只有sku,直接赋值主商品值
            Map<String, Object> map = getSkuInfo(cookies, dataSpu);
            Sku item = new Sku();
            item.setId(dataSpu).setSpuId(dataSpu).setName(name).setImage(product.getImage()).setImages(product.getImage())
                    .setPaymoney(judge(map.get("p").toString(),price)).setBrandName(brandName)
                    .setPrice(judge(map.get("op").toString(),price))
                    .setSn(idWorker.nextId()+"").setNum(RandomUtil.randomInt(2000)+100);;
            items.add(item);
        }

        /**
         * 爬取商品介绍，由于是动态加载，先用htmlunit加载完整个页面，再用jsoup爬取
         */
        String caption = document.select(".itemInfo-wrap .news").text();
        Elements intro = document.select("#J-detail-content");
        Elements imgs = intro.select("img");
        for(Element img : imgs){
            String src = img.attr("data-lazyload");
            if(ossStorage){ //如果需要存七牛云的话，爬取的img src要换成七牛云的
                String picName = src.substring(src.lastIndexOf("/")+1);
                img.attr("data-lazyload",ossUrl + ossPath+picName);
                img.attr("src",ossUrl + ossPath+picName);
                httpUtils.downloadNetImage(src,storage, ossStorage);
            }else {
                img.attr("src",src);
                if(StringUtils.isNotEmpty(storage)) //开启了本地存储
                    httpUtils.downloadNetImage(src,storage, ossStorage);
            }
        }
        product.setName(name).setCaption(caption).setIntroduction(intro.html()).setSn(idWorker.nextId()+"");
        log.info("\n###### spu name:{}, caption:{}",name, caption);

        if(CollectionUtils.isEmpty(items)) throw new RuntimeException("商品爬取失败，请重新选择链接地址");
        //子商品去重
        log.info("\n###### 去重前 item total:{}", items.size());
        //根据id去重
        items = items.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
                new TreeSet<Sku>(Comparator.comparing(Sku::getId))), ArrayList::new));
        log.info("\n###### 去重后 item total:{}", items.size());

        product.setStatus("1");
        productService.saveOrUpdate(product);
        skuService.saveOrUpdateBatch(items);
        log.info("\n#################################################");
        log.info("\n###### 爬取数据成功：spuId:{},sku total:{}", dataSpu, items.size());
        log.info("\n#################################################");
        String msg = "爬取数据成功：spuId:"+dataSpu+",子商品数量:"+items.size();
        return msg;
    }

    @Async
    public void updateItem(String id, String cookies, String ip){
        String url = "https://item.jd.com/"+id+".html";
        String msg = crawItem(url, cookies, null, true);
        //记录日志
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
     * 获取详情页面
     * @param ck cookie值
     * @param id
     * @return
     */
    public Map<String, Object> getSkuInfo(String ck,String id){
        log.info("\n###### getSkuInfo id:{}",id);
        Map<String, Object> map = new HashMap<>();

        HttpHeaders headers = new HttpHeaders();//header参数
        List<String> cookies = Arrays.asList(ck.split(";"));
        // header设置
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36");
        // cookie设置
        headers.put(HttpHeaders.COOKIE, cookies);

        HttpEntity<String> httpEntity = new HttpEntity(headers);

        Document document = null;
        try {
            document = Jsoup.parse(httpUtils.parseById(ck, id));
        } catch (Exception e) {
            e.printStackTrace();
            log.info("\n###### ",e);
            throw new RuntimeException("Jsoup 解析页面出错");
        }
        String title = document.select(".product-intro .sku-name").text(); //商品名称

        //设置商品的图片
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

    //http请求查询价格
    public Map<String, Object> queryPrice(String ck, String id){
        HttpHeaders headers = new HttpHeaders();//header参数
        List<String> cookies = Arrays.asList(ck.split(";"));
        // header设置
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36");
        // cookie设置
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
            log.info("\n###### 获取商品价格异常：",e.getMessage());
            map.put("p",0);
            map.put("op",0);
        }
        return map;
    }
    
    //评论处理
    private int handbleComment(String comment){
        log.info("\n###### handbleComment num:{}", comment);
        if(comment.contains("万+"))
            return new BigDecimal(comment.replace("万+", "")).movePointRight(4).intValue() + RandomUtil.randomInt(100);
        else
            return Integer.parseInt(comment.replace("+","")) + RandomUtil.randomInt(100);
    }

    //将主商品的价格和爬取的价格做判断，子商品价格为0，表示爬取价格有误，默认赋值主商品价格
    private Integer judge(String price, Integer p){
        if("0".equals(price)) return p;
        else return Integer.parseInt(price);
    }


    /**
     * 爬虫多线程
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
            log.info("\n#####################【多线程 CrawlerItem 爬取子商品】#########################################################");
            Sku item = null;
            try {
                item = new Sku();
                String skuId = spec.attr("data-sku");
                Map<String, Object> map = getSkuInfo(cookies, skuId);
                String imgUrl = map.get("picUrl").toString();
                String picName = imgUrl.substring(imgUrl.lastIndexOf("/")+1);
                //数据库对应的图片链接
                String dbImg = ossStorage ? ossUrl + ossPath+picName : imgUrl;
                item.setId(skuId).setSpuId(dataSpu).setName(map.get("title").toString())
                        .setImage(dbImg).setImages(dbImg).setPaymoney(judge(map.get("p").toString(),price))
                        .setPrice(judge(map.get("op").toString(), price)).setBrandName(brandName).setCommentNum(commentTotal)
                        .setSn(idWorker.nextId()+"").setNum(RandomUtil.randomInt(2000)+100)
                        .setCategoryId(categoryId).setCategoryName(categoryName);
                Boolean pstatus = httpUtils.downloadNetImage(imgUrl, storage, ossStorage);
                item.setPstatus(pstatus == true ? 1: 0);
            } catch (Exception e) {
                log.info("\n###### CrawlerItem 爬取异常：{}",e);
            }
           return item;
        }
    }

    /**
     * 爬虫多线程
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
            log.info("\n#####################【多线程 CrawlerList 爬取子商品】#########################################################");
            String msg = null;
            try {
                msg = crawItem(url, cookies, storage, false);
            } catch (Exception e) {
                log.info("\n###### CrawlerItem 爬取异常：{}",e);
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



