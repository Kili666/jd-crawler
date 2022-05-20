package com.lqjai.crawler.controller;

import com.lqjai.common.utils.R;
import com.lqjai.crawler.pojo.CrawlerLog;
import com.lqjai.crawler.service.CrawlerLogService;
import com.lqjai.crawler.service.SkuService;
import com.lqjai.crawler.task.JdCrawlerTask;
import com.lqjai.crawler.util.FileUtil;
import com.lqjai.crawler.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("jdcrawler")
@Slf4j
public class JdCrawlerController {
    @Autowired
    private JdCrawlerTask crawlerTask;
    @Autowired
    private HttpUtils httpUtils;
    @Autowired
    private SkuService skuService;
    @Autowired
    private CrawlerLogService crawlerLogService;
    @Autowired
    private FileUtil fileUtil;

    @GetMapping ("test")
    public R test(){
        return R.ok();
    }

    /**
     * 爬取商品列表
     * @param params
     * @return
     */
    @PostMapping("list")
    public R crawList(HttpServletRequest request, @RequestBody Map<String,String> params){
        String msg = crawlerTask.crawList(params.get("listUrl"), params.get("cookies"), fileUtil.getStoragePath());
        //记录日志
        CrawlerLog crawlerLog = new CrawlerLog().setType(2).setMsg(msg).setUrl(params.get("listUrl")).setOperator(request.getRemoteHost());
        crawlerLogService.save(crawlerLog);
        return R.ok(msg);
    }

    /**
     * 爬取单个商品
     * @param params
     * @return
     */
    @PostMapping("item")
    public R crawItem(HttpServletRequest request, @RequestBody Map<String, String> params){
        String msg = crawlerTask.crawItem(params.get("itemUrl"), params.get("cookies"), fileUtil.getStoragePath(), false);
        //记录日志
        CrawlerLog crawlerLog = new CrawlerLog().setType(1).setMsg(msg).setUrl(params.get("itemUrl")).setOperator(request.getRemoteHost());
        crawlerLogService.save(crawlerLog);
        return R.ok(msg);
    }

    @PutMapping("item")
    public R updateItem(HttpServletRequest request, @RequestBody Map<String, String> params){
        log.info("\n###### updateItem params:{}",params);
        Integer pstatus = skuService.getById(params.get("id")).getPstatus();
        if(pstatus == 1) throw new RuntimeException("该商品问题不大，请重新爬取其他数据异常的商品");
        crawlerTask.updateItem(params.get("id"), params.get("cookies"), request.getRemoteHost());
       return R.ok("已提交申请，请稍等片刻后刷新");
    }

}
