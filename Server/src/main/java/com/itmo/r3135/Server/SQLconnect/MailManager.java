package com.itmo.r3135.Server.SQLconnect;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

/**
 * Класс для работы с почтовым сервером
 */
public class MailManager {
    static final Logger logger = LogManager.getLogger("MailManager");
    private Session session;
    private String username;
    private String password;
    private String host;
    private int port;
    private boolean auth;

    /**
     * @param username Аккаунт, с которого отправляется почта
     * @param password Пароль пользователя
     * @param host     Адрес почтового сервера
     * @param port     Порт почтового сервера
     * @param auth
     */
    public MailManager(String username, String password, String host, int port, boolean auth) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.auth = auth;
    }

    /**
     * Инициализиреут точтовый клиент
     *
     * @return Статус инициализации
     */
    public boolean initMail() {
        logger.info("Mail Manager connect...");
        try {
            Properties prop = new Properties();
            prop.put("mail.smtp.host", host);
            prop.put("mail.smtp.port", port);
            prop.put("mail.smtp.auth", auth);
            prop.put("mail.smtp.socketFactory.port", port);
            prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            session = Session.getInstance(prop,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });
        } catch (Exception e) {
            logger.fatal("Mail Manager ERROR!");
            return false;
        }
        logger.info("Mail good connect!");

        return true;
    }

    /**
     * Оправляет постое письмо на указанный ардес
     *
     * @param eMail Адрес получателя
     * @return Статус отправки
     */
    public boolean sendMail(String eMail) {

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(eMail)
            );
            message.setSubject("MESSAGE");
            message.setText("Message,"
                    + "\n\n Message!");
            Transport.send(message);

        } catch (MessagingException e) {
            logger.error("Send email message error!");
            return false;
        }
        return true;
    }

    /**
     * Оправляет письмо-подтверждение, созданное по HTML-шаблону на указанный ардес
     *
     * @param eMail Адрес получателя
     * @param login Имя пользователя
     * @param code  Проверочный код
     * @return Статус отправки
     */
    public boolean sendMailHTML(String eMail, String login, String code) {
        String htmlText;
        String htmlFileName = "emailTemplate.html";

        try (InputStream htmlSrteam = getClass().getClassLoader().getResourceAsStream(htmlFileName);
             Scanner s = new Scanner(htmlSrteam).useDelimiter("\\A")) {
            htmlText = s.hasNext() ? s.next() : "";
            htmlText = htmlText.replace("insertLogin", login);
            htmlText = htmlText.replace("insertEmail", eMail);
            htmlText = htmlText.replace("verificationCode", code);
        } catch (
                Exception e) {
            logger.error("Error in html file read!");
            return false;
        }
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(eMail)
            );
            message.setSubject("Thank you for registering in our database");

            message.setContent(htmlText, "text/html; charset=utf-8");
            Transport.send(message);

        } catch (
                MessagingException e) {
            logger.error("Send email message error!");
            return false;
        }
        return true;
    }
}
