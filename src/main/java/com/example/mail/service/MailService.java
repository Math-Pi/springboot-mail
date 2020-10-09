package com.example.mail.service;

/**
 * Created by summer on 2017/5/4.
 */
public interface MailService {
    //简单邮件
    void sendSimpleMail(String to, String subject, String content);
    //发送网页
    void sendHtmlMail(String to, String subject, String content);

    void sendAttachmentsMail(String to, String subject, String content, String filePath);

    void sendInlineResourceMail(String to, String subject, String content, String rscPath, String rscId);

}
