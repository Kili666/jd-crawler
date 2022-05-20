package com.lqjai.crawler.util;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
* FileUtil
*
*@Author Kili
*@Date 2022/4/21 12:54
*/
@Slf4j
@Component
public class FileUtil {

    @Value("${qiniu.storage}")
    public Boolean storage;
    @Value("${qiniu.ossStorage}")
    public Boolean ossStorage;

    public Boolean saveFile(String path,String name,String
            suffix, String content){
        File file = new File(path);
        if(!file.exists()) file.mkdir();
        if(StringUtils.isEmpty(suffix)) suffix = "txt";
        if(StringUtils.isEmpty(name) || StringUtils.isEmpty(content))
            throw new RuntimeException("文件名或内容不能为空");
        try {
            FileWriter myWriter = new FileWriter(new File(file.getPath() + File.separator +name+"."+suffix));
            myWriter.write(content);
            myWriter.close();
            log.info("\nSuccessfully wrote to the file.");
        } catch (IOException e) {
            log.info("\nAn error occurred.");
            e.printStackTrace();
        }
        return true;
    }

    public Boolean saveFile(String name, String content){
        return saveFile(null,name,null, content);
    }

    public String tackFilePath(String path){
        File file = new File(path);
        if(!file.exists()) file.mkdir();
        return file.getPath();
    }

    /**
     * 删除文件或目录
     * @param FileName 文件或文件夹的路径
     * @return true or false 成功返回true，失败返回false
     */
    public boolean deleteFileOrDir(String FileName){
        File file = new File(FileName);//根据指定的文件名创建File对象
        if (!file.exists()){  //要删除的文件不存在
            log.info("\n###### 文件"+FileName+"不存在，删除失败！");
            return false;
        }else{ //要删除的文件存在
            if ( file.isFile() ){ //如果目标文件是文件
                return deleteFile(FileName);
            }else{  //如果目标文件是目录
                return deleteDir(FileName);
            }
        }
    }

    /**
     * 删除指定文件
     * @param fileName 文件路径
     * @return true or false 成功返回true，失败返回false
     */
    public boolean deleteFile(String fileName){
        File file = new File(fileName);//根据指定的文件名创建File对象
        if (  file.exists() && file.isFile() ){ //要删除的文件存在且是文件
            if ( file.delete()){
                log.info("\n###### 文件"+fileName+"删除成功！");
                return true;
            }else{
                log.info("\n###### 文件"+fileName+"删除失败！");
                return false;
            }
        }else{
            log.info("\n###### 文件"+fileName+"不存在，删除失败！");
            return false;
        }
    }

    /**
     * 删除指定的目录以及目录下的所有子文件
     * @param dirName is 目录路径
     * @return true or false 成功返回true，失败返回false
     */
    public boolean deleteDir(String dirName){
        if ( dirName.endsWith(File.separator) )//dirName不以分隔符结尾则自动添加分隔符
            dirName = dirName + File.separator;
        File file = new File(dirName);//根据指定的文件名创建File对象
        if ( !file.exists() || ( !file.isDirectory() ) ){ //目录不存在或者
            log.info("\n###### 目录删除失败" +dirName+"目录不存在！");
            return false;
        }
        File[] fileArrays = file.listFiles();//列出源文件下所有文件，包括子目录
        for ( int i = 0 ; i < fileArrays.length ; i++ ){//将源文件下的所有文件逐个删除
            deleteFileOrDir(fileArrays[i].getAbsolutePath());
        }
        if ( file.delete() )//删除当前目录
            log.info("\n###### 目录"+dirName+"删除成功！" );
        return true;
    }

    public void renameFile(String filePath, String oldName, String newName) {
        String prefix = filePath.split(oldName)[0];
        File oldFile = new File(filePath);
        String newFileName = prefix + newName;
        File newFile = new File(newFileName);
        if (oldFile.exists() && oldFile.isFile()) {
            oldFile.renameTo(newFile);
        }
    }

    /**
     * 获取当前用户目录，如果是Linux，路径设为空
     * @return
     */
    public String getStoragePath(){
        log.info("\n###### storage:{}, ossStorage:{}", storage, ossStorage);
        if(storage){
            String userDir = System.getProperty("user.dir");
            log.info("\n###### userDir:{}",userDir);
            return userDir;
        } else return null;
    }


}
