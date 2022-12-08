## 部署

```shell
/**
 * java -jar xxx.jar # 这个命令会锁定命令窗口，当窗口关闭时，程序也就自动退出了，所以需要让 jar 包后台运行
 * nohup java -jar xxx.jar & # nohup 命令：忽略所有挂断信号，当窗口关闭时，程序仍然运行, & 符号：程序后台运行
 * # xxx.log 就是指定的输出文件，如果不指定，默认在 jar 包所在目录，创建 nohup.out 文件
 * nohup java -jar xxx.jar >xxx.log & # >xxx.file：将输出重定向到 xxx.file 文件，也就是将内容输出到 xxx.file 文件中
 */
 mkdir -p /mydata/gmall
 
 cd /mydata/gmall/
 
 nohup java -jar gmall-admin.jar >admin.log &
```

![](https://oss.yiki.tech/oss/202212080138216.png)

![](https://oss.yiki.tech/oss/202212080142840.png)

![](https://oss.yiki.tech/oss/202212080147040.png)

![](https://oss.yiki.tech/oss/202212080149435.png)
