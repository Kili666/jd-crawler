package com.lqjai.crawler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lqjai.common.utils.PageResult;
import com.lqjai.common.utils.QiniuUtils;
import com.lqjai.crawler.pojo.Sku;
import com.lqjai.crawler.pojo.Spu;
import com.lqjai.crawler.service.CategoryService;
import com.lqjai.crawler.service.SkuService;
import com.lqjai.crawler.service.SpuService;
import com.lqjai.crawler.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
@Slf4j
public class MainTest {
    @Autowired
    private HttpUtils httpUtils;
    @Autowired
    private QiniuUtils qiniuUtils;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SkuService skuService;
    @Autowired
    private SpuService spuService;
    @Value("${qiniu.ossStorage}")
    public Boolean ossStorage;

    // 任务执行器
    private ExecutorService executorService;

    // 初始化线程池
    @PostConstruct
    private void initThreadPool() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.DiscardPolicy());
        }
    }

    @Test
    public void testGetImg(){
        String url = "//img10.360buyimg.com/n1/jfs/t1/200865/20/21899/100899/62626477E7b1f6454/947f1120be0c9888.jpg.avif";
        httpUtils.downloadNetImage(url,"F:\\temp\\crawler", true);
        System.out.println("\n###### 下载完成");
    }

    @Test
    public void testDelImg(){
        qiniuUtils.deleteFileFromQiniu("qjmall/img/goods/f58d3440b5d80b90.jpg");
        System.out.println("删除成功");
    }

    @Test
    public void getSystem(){
        String property = System.getProperty("user.dir");
        System.out.println(property);
    }

    /**
     * 导入子商品到 ES
     */
    @Test
    public void initGoods() {
        log.info("\n#####################【多线程 initGoods】#########################################################");
        //计算出总页数
        QueryWrapper<Spu> select = new QueryWrapper<Spu>().select("id", "category1_id");
        List<Spu> list = spuService.list(select);
        list.forEach(item->{
            List<Sku> skuList = skuService.list(Wrappers.<Sku>lambdaQuery().eq(Sku::getSpuId, item.getId()));
            skuList.forEach(sku->{
                sku.setCategoryId(item.getCategory1Id()).setCategoryName(categoryService.getById(item.getCategory1Id()).getName());
            });
            skuService.updateBatchById(skuList);
            log.info("\n###### 更新spuId：{}子商品评论数,size:{}", item.getId(), skuList.size());
        });

        log.info("\n#####################【initGoods finished】#########################################################");
    }

    /**
     * 修复spu intro懒加载bug
     */
    @Test
    public void reviewSpu() {
        log.info("\n#####################【多线程 reviewSpu】#########################################################");
        //计算出总页数
        QueryWrapper<Spu> select = new QueryWrapper<Spu>().select("id");
        List<Spu> list = spuService.list(select);
        AtomicInteger i = new AtomicInteger();
        list.forEach(item ->{
            if(StringUtils.isNotEmpty(item.getIntroduction())){
                Document document = Jsoup.parse(item.getIntroduction());
                Elements imgs = document.select("img");
                for(Element img : imgs){
                    String src = img.attr("data-lazyload");
                    img.attr("src",src);
                }
                spuService.updateById(item.setIntroduction(document.toString()));
            }
            log.info("\n###### 已处理第{}条数据", i.getAndIncrement());
        });
        log.info("\n#####################【reviewSpu finished】#########################################################");
    }

    /**
     * 爬虫多线程
     *
     *@Author Kili
     *@Date 2022/4/22 22:53
     */
    private class ImportSkuProcessor implements Callable<Void> {
        private Integer pageNum;
        private Integer pageSize;
        private Integer totalPage;

        public ImportSkuProcessor(Integer pageNum, Integer pageSize, Integer totalPage) {
            this.pageNum = pageNum;
            this.pageSize = pageSize;
            this.totalPage = totalPage;
        }


        @Override
        public Void call() {
            try {
                PageResult page = spuService.findPage(new Spu().setStatus("1"), pageNum, pageSize);
                List<Spu> spuList = page.getRows();
                for (Spu spu : spuList) {
                    List<Sku> list = skuService.list(Wrappers.<Sku>lambdaQuery().eq(Sku::getSpuId, spu.getId()));
                    list.forEach(item->{
                        item.setCommentNum(spu.getCommentNum());
                    });
                    skuService.saveBatch(list);
                }
                log.info("\n###### 已存入第 【{}/{}】 页数据", pageNum, totalPage);
            } catch (Exception e) {
                log.info("\n###### ImportSkuProcessor 导入ES异常：{}",e);
            }
            return null;
        }
    }

    /**
     * 爬虫多线程
     *
     *@Author Kili
     *@Date 2022/4/22 22:53
     */
    private class HandleSpuIntro implements Callable<Spu> {
        private Spu spu;

        public HandleSpuIntro(Spu spu) {
            this.spu = spu;
        }


        @Override
        public Spu call() {
            try {
                Document document = Jsoup.parse(spu.getIntroduction());
                Elements imgs = document.select("img");
                for(Element img : imgs){
                    String src = img.attr("data-lazyload");
                    img.attr("src",src);
                }
                return spu.setIntroduction(document.toString());
            } catch (Exception e) {
                log.info("\n###### HandleSpuIntro 导入ES异常：{}",e);
            }
            return null;
        }
    }



}
