package com.lqjai.crawler.util;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.lqjai.common.utils.QiniuUtils;
import com.lqjai.crawler.config.HUnitCssErrorListener;
import com.lqjai.crawler.config.HUnitJSErrorListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Component
@Slf4j
public class HttpUtils {
    @Autowired
    private QiniuUtils qiniuUtils;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private FileUtil fileUtil;
    @Value("${qiniu.file.path}")
    public String ossPath;

    /**
     * 下载网络图片存储到本地，并上传至七牛云，http上传和下载比较耗时，使用异步线程
     * @param url
     * @param storage 图片存储路径
     * @return 返回的Boolean为false表示图片来源第三方，链接有效性未验证。为true表示已存储至自己的oss,长期稳定有效
     */
//    @Async
    public Boolean downloadNetImage(String url, String storage, Boolean ossStorage){
        //声明获取请求方式get，设置URL地址
        if(!url.contains("http://") && !url.contains("https://")) //防止有的地址省略了协议
            url = "https:" + url;
        log.info("\n###### downloadNetImage url:{}", url);
        //获取图片的后缀
        String picName = url.substring(url.lastIndexOf("/")+1);
        /**
         * 本地storage和oss存储，只要有一个为true，就需要下载网络文件
         */
        byte[] bytes = null;
        if(StringUtils.isNotEmpty(storage) || ossStorage){
            log.info("\n###### 开始下载网络文件：{}",url);
            bytes = downloadNetworkFile(url);
        }
        //文件保存到本地
        if(StringUtils.isNotEmpty(storage)){
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(fileUtil.tackFilePath(storage + File.separator + "img") + File.separator + picName);
                fileOutputStream.write(bytes);
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                log.info("\n###### ",e);
            }
        }
        //判断文件是否保存到oss
        if(ossStorage){
            String msg = null;
            try {
                msg = qiniuUtils.uploadByByte(bytes, ossPath + picName);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                log.info("\n###### 文件上传至七牛云失败");
                if(StringUtils.isNotEmpty(storage))
                    fileUtil.saveFile(storage + File.separator + "log" , "qiniu-"+url.substring(url.lastIndexOf("/")+1), "txt", msg);
                return false;
            }
        }
       return false;
    }

    public byte[] downloadNetworkFile(String httpFileUrl) {
        long begin = System.currentTimeMillis();
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36");
        ResponseEntity<byte[]> response = restTemplate.exchange(httpFileUrl, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
        byte[] fileBytes = response.getBody();
        long end = System.currentTimeMillis();
        log.info("下载文件 httpFileUrl:{} 花费时长:{}ms", httpFileUrl, (end - begin));
        return fileBytes;
    }

    /**
     *
     * @param ck cookie
     * @throws Exception
     */
    public String parseById(String ck, String id) throws Exception{
        // 得到浏览器对象，直接New一个就能得到，现在就好比说你得到了一个浏览器了
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.setJavaScriptErrorListener(new HUnitJSErrorListener());
        webClient.setCssErrorHandler(new HUnitCssErrorListener());
        webClient.setJavaScriptTimeout(30000);

        // 这里是配置一下不加载css和javaScript，因为httpunit对javascript兼容性不太好
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);

        Cookie cookie = new Cookie(".jd.com","Cookie",ck);
        webClient.getCookieManager().addCookie(cookie);
        // header设置
        webClient.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36");
        // 做的第一件事，去拿到这个网页，只需要调用getPage这个方法即可
        HtmlPage htmlpage = webClient.getPage("https://item.jd.com/"+id+".html");
        return htmlpage.asXml();
    }

    /**
     * @param cookie
     * @param url 爬取链接
     * @return
     * @throws Exception
     */
    public  String parseByUrl(String cookie, String url) throws Exception{
        // 得到浏览器对象，直接New一个就能得到，现在就好比说你得到了一个浏览器了
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.setJavaScriptErrorListener(new HUnitJSErrorListener());
        webClient.setCssErrorHandler(new HUnitCssErrorListener());
        webClient.setJavaScriptTimeout(30000);

        // 这里是配置一下不加载css和javaScript，因为httpunit对javascript兼容性不太好
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);

        Cookie ck = new Cookie(".jd.com","Cookie",cookie);
        webClient.getCookieManager().addCookie(ck);
        // header设置
        webClient.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36");
        // 做的第一件事，去拿到这个网页，只需要调用getPage这个方法即可
        HtmlPage htmlpage = webClient.getPage(url);
        return htmlpage.asXml();
    }

}
