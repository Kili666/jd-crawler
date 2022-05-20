package com.lqjai.common.utils;

import com.alibaba.fastjson.JSON;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;

/**
 * 七牛云工具类
 */
@Slf4j
@Component
public class QiniuUtils {
    @Value("${qiniu.accessKey}")
    public String accessKey;
    @Value("${qiniu.secretKey}")
    public String secretKey;
    @Value("${qiniu.bucket}")
    public String bucket;

    //带指定Zone对象的配置类
    private Configuration cfg;
    private UploadManager uploadManager;
    private Auth auth;

    @PostConstruct
    private void init(){
        //构造一个带指定Zone对象的配置类
        cfg = new Configuration(Region.region0());
        //...其他参数参考类注释
        uploadManager = new UploadManager(cfg);
        auth = Auth.create(accessKey, secretKey);
    }

    /**
     * 上传单文件
     *
     * @param filePath 要上传的文件路径（包含文件名）
     * @param fileName 上传到七牛云后的文件名
     */
    public void uploadByPath(String filePath, String fileName) {
        String upToken = auth.uploadToken(bucket);
        try {
            Response response = uploadManager.put(filePath, fileName, upToken);
            //解析上传成功的结果
            DefaultPutRet putRet = JSON.parseObject(response.bodyString(), DefaultPutRet.class);
            log.info("\n###### 上传文件成功，文件名：" + fileName);
        } catch (QiniuException ex) {
            Response r = ex.response;
            try {
                log.info("\n###### "+r.bodyString());
            } catch (QiniuException ex2) {
                //ignore
            }
        }
    }

    //上传字符文件
    //上传字符文件
    public String uploadByByte(byte[] bytes, String fileName) {
        String upToken = auth.uploadToken(bucket);
        //默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = fileName;
        try {
            Response response = uploadManager.put(bytes, key, upToken);
            //解析上传成功的结果
            DefaultPutRet putRet = JSON.parseObject(response.bodyString(), DefaultPutRet.class);
            log.info("\n###### 上传文件成功，文件名：" + fileName);
            return putRet.toString();
        } catch (QiniuException ex) {
            Response r = ex.response;
            log.info("\n###### "+r.toString());
            try {
                log.info("\n###### "+r.bodyString());
            } catch (QiniuException ex2) {
                //ignore
            }
            return r.toString();
        }
    }

    public void uploadByStream(ByteArrayInputStream byteInputStream, String fileName){
        String upToken = auth.uploadToken(bucket);
        try {
            Response response = uploadManager.put(byteInputStream, fileName,upToken,null, null);
            //解析上传成功的结果
            DefaultPutRet putRet = JSON.parseObject(response.bodyString(), DefaultPutRet.class);
            System.out.println(putRet.key);
            System.out.println(putRet.hash);
        } catch (QiniuException ex) {
            Response r = ex.response;
            log.info("\n###### "+r.toString());
            try {
                log.info("\n###### "+r.bodyString());
            } catch (QiniuException ex2) {
                //ignore
            }
        }
    }

    //删除文件
    public void deleteFileFromQiniu(String fileName) {
        //构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(Region.region0());
        String key = fileName;
        Auth auth = Auth.create(accessKey, secretKey);
        BucketManager bucketManager = new BucketManager(auth, cfg);
        try {
            bucketManager.delete(bucket, key);
            log.info("\n###### 删除文件成功，文件名：" + fileName);
        } catch (QiniuException ex) {
            //如果遇到异常，说明删除失败
            log.info("\n###### "+ex.code());
            log.info("\n###### "+ex.response.toString());
        }
    }

}
