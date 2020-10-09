# SpringBoot-Mail

## SimpleMailMessage

### 1、pom.xml引入Maven依赖

```xml
<dependency> 
    <groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-mail</artifactId>
</dependency> 
```

### 2、application.properties 中添加邮箱配置

```properties
spring.mail.host=smtp.qq.com //邮箱服务器地址
spring.mail.username=xxx@qq.com //用户名
spring.mail.password=xxx    //密码(授权码)
spring.mail.default-encoding=UTF-8	//编码方式

mail.fromMail.addr=xxx@qq.com  //以谁来发送邮件
```

### 3、MailService接口

```java
public interface MailService {
    void sendSimpleMail(String to, String subject, String content);
}
```

### 4、MailServiceImpl实现类

- **JavaMailSender**接口：发送邮件
- **SimpleMailMessage**：发送简单的文本

```java
@Component
public class MailServiceImpl implements MailService{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private JavaMailSender mailSender;
    @Value("${mail.fromMail.addr}")
    private String from;
    @Override
    public void sendSimpleMail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        try {
            mailSender.send(message);
            logger.info("简单邮件已经发送。");
        } catch (Exception e) {
            logger.error("发送简单邮件时发生异常！", e);
        }
    }
}
```

### 5、测试类

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class MailServiceTest {
    @Autowired
    private MailService MailService;
    @Test
    public void testSimpleMail() throws Exception {
        //此处为收件人邮箱
        MailService.sendSimpleMail("3224961708@qq.com","test simple mail"," hello this is simple mail");
    }
}
```

## MimeMessage

- 加入图片或者附件

### 发送 html 格式邮件

#### 实现方法

```java
public void sendHtmlMail(String to, String subject, String content) {
    MimeMessage message = mailSender.createMimeMessage();

    try {
        //true表示需要创建一个multipart message
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(message);
        logger.info("html邮件发送成功");
    } catch (MessagingException e) {
        logger.error("发送html邮件时发生异常！", e);
    }
}
```

#### 测试方法

```java
@Test
public void testHtmlMail() throws Exception {
    String content="<html>\n" +
            "<body>\n" +
            "    <h3>hello world ! 这是一封Html邮件!</h3>\n" +
            "</body>\n" +
            "</html>";
    MailService.sendHtmlMail("3224961708@qq.com","test simple mail",content);
}
```

### 发送带附件的邮件

#### 实现方法

- **File.separator**：代表系统目录中的间隔符，说白了就是斜线。
- **lastIndexOf()**：返回指定字符在此字符串中最后一次出现处的索引
- **substring()** 方法返回字符串的子字符串。

```java
public void sendAttachmentsMail(String to, String subject, String content, String filePath){
    MimeMessage message = mailSender.createMimeMessage();
    try {
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);
        FileSystemResource file = new FileSystemResource(new File(filePath));
        String fileName = filePath.substring(filePath.lastIndexOf(File.separator));
        helper.addAttachment(fileName, file);
        mailSender.send(message);
        logger.info("带附件的邮件已经发送。");
    } catch (MessagingException e) {
        logger.error("发送带附件的邮件时发生异常！", e);
    }
}
```

- **注意：添加多个附件可以使用多条 `helper.addAttachment(fileName, file)`**

#### 测试方法

```java
@Test
public void sendAttachmentsMail() {
    String filePath="e:\qq.jpg";
    mailService.sendAttachmentsMail("3224961708@qq.com", "主题：带附件的邮件", "有附件，请查收！", filePath);
}
```

### 发送带静态资源的邮件

#### 实现方法

```java
public void sendInlineResourceMail(String to, String subject, String content, String rscPath, String rscId){
    MimeMessage message = mailSender.createMimeMessage();
    try {
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);
        FileSystemResource res = new FileSystemResource(new File(rscPath));
        helper.addInline(rscId, res);
        mailSender.send(message);
        logger.info("嵌入静态资源的邮件已经发送。");
    } catch (MessagingException e) {
        logger.error("发送嵌入静态资源的邮件时发生异常！", e);
    }
}
```

#### 测试方法

```java
@Test
public void sendInlineResourceMail() {
    String rscId = "neo006";
    String content="<html><body>这是有图片的邮件：<img src=\'cid:" + rscId + "\' ></body></html>";
    String imgPath = "e:\qq.jpg";
    mailService.sendInlineResourceMail("3224961708@qq.com", "主题：这是有图片的邮件", content, imgPath, rscId);
}
```

- **注意：添加多个图片可以使用多条 `<img src='cid:" + rscId + "' >` 和 `helper.addInline(rscId, res)` 来实现** 

## 邮件系统

### 1、pom 中导入 thymeleaf 的包

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

### 2、在 resorces/templates 下创建 emailTemplate.html 

```html
<!DOCTYPE html>
<html lang="zh" xmlns:th="http://www.thymeleaf.org">
    <head>
        <meta charset="UTF-8"/>
        <title>Title</title>
    </head>
    <body>
        您好,这是验证邮件,请点击下面的链接完成验证,<br/>
        <a href="#" th:href="@{ http://www.ityouknow.com/neo/{id}(id=${id}) }">激活账号</a>
    </body>
</html>
```

### 3、测试类

```java
@Test
public void sendTemplateMail() {
    //创建邮件正文
    Context context = new Context();
    context.setVariable("id", "006");
    String emailContent = templateEngine.process("emailTemplate", context);
    mailService.sendHtmlMail("3224961708@qq.com","主题：这是模板邮件",emailContent);
}
```