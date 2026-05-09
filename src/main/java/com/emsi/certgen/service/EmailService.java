package com.emsi.certgen.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
@Slf4j
public class EmailService {

    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    @Value("${sendgrid.from-name}")
    private String fromName;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /* ── Email de réinitialisation ── */
    public void sendPasswordResetEmail(String toEmail, String toName, String resetToken) {
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
        sendEmail(toEmail, toName,
                "Réinitialisation de votre mot de passe — EMSI",
                buildResetText(toName, resetLink),
                buildResetHtml(toName, resetLink));
    }

    /* ── Email de bienvenue avec lien vers le site ── */
    public void sendWelcomeEmail(String toEmail, String toName, String username, String tempPassword) {
        String loginLink = frontendUrl + "/login";
        sendEmail(toEmail, toName,
                "Bienvenue sur EMSI Certificate Generator",
                buildWelcomeText(toName, username, tempPassword, loginLink),
                buildWelcomeHtml(toName, username, tempPassword, loginLink));
    }

    private void sendEmail(String toEmail, String toName, String subject, String text, String html) {
        Email from = new Email(fromEmail, fromName);
        Email to   = new Email(toEmail, toName);
        Mail mail  = new Mail(from, subject, to, new Content("text/plain", text));
        mail.addContent(new Content("text/html", html));

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("✅ Email envoyé à {}", toEmail);
            } else {
                log.error("❌ SendGrid: {} — {}", response.getStatusCode(), response.getBody());
            }
        } catch (IOException e) {
            log.error("❌ Erreur email: {}", e.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'email: " + e.getMessage());
        }
    }

    /* ════════════════════════
       TEMPLATES HTML
    ════════════════════════ */
    private String buildResetHtml(String name, String link) {
        return """
        <!DOCTYPE html><html><head><meta charset="UTF-8"></head>
        <body style="font-family:Arial,sans-serif;background:#FDFAF3;margin:0;padding:0;">
          <div style="max-width:600px;margin:40px auto;background:#fff;border-radius:12px;border:1px solid rgba(184,134,11,0.2);overflow:hidden;">
            <div style="background:linear-gradient(135deg,#1A1208,#2E1E08);padding:32px;text-align:center;">
              <h1 style="color:#D4A843;font-size:24px;margin:0;letter-spacing:2px;">EMSI</h1>
              <p style="color:rgba(212,168,67,0.7);font-size:12px;margin:4px 0 0;">Service E-Learning — Certificate Generator</p>
            </div>
            <div style="padding:40px 32px;">
              <h2 style="color:#1A1208;">Bonjour %s,</h2>
              <p style="color:#3D2B0E;line-height:1.7;">Vous avez demandé la réinitialisation de votre mot de passe.</p>
              <div style="text-align:center;margin:32px 0;">
                <a href="%s" style="background:linear-gradient(135deg,#7A5800,#B8860B);color:white;padding:14px 32px;border-radius:8px;text-decoration:none;font-weight:bold;font-size:16px;display:inline-block;">
                  🔐 Réinitialiser mon mot de passe
                </a>
              </div>
              <p style="color:#7A5C30;font-size:13px;">⏱️ Ce lien expire dans <strong>2 heures</strong>.<br>Si vous n'avez pas fait cette demande, ignorez cet email.</p>
            </div>
            <div style="background:#F5EDD8;padding:20px 32px;text-align:center;border-top:1px solid rgba(184,134,11,0.2);">
              <p style="color:#B89870;font-size:12px;margin:0;">© EMSI — École Marocaine des Sciences de l'Ingénieur</p>
            </div>
          </div>
        </body></html>
        """.formatted(name, link);
    }

    private String buildResetText(String name, String link) {
        return "Bonjour " + name + ",\n\nRéinitialisez votre mot de passe :\n" + link +
               "\n\nCe lien expire dans 2 heures.\n\nEMSI — Service E-Learning";
    }

    private String buildWelcomeHtml(String name, String username, String pwd, String loginLink) {
        return """
        <!DOCTYPE html><html><head><meta charset="UTF-8"></head>
        <body style="font-family:Arial,sans-serif;background:#FDFAF3;margin:0;padding:0;">
          <div style="max-width:600px;margin:40px auto;background:#fff;border-radius:12px;border:1px solid rgba(184,134,11,0.2);overflow:hidden;">
            <div style="background:linear-gradient(135deg,#1A1208,#2E1E08);padding:32px;text-align:center;">
              <h1 style="color:#D4A843;font-size:24px;margin:0;">Bienvenue chez EMSI ! 🎓</h1>
              <p style="color:rgba(212,168,67,0.7);font-size:12px;margin:4px 0 0;">Certificate Generator — Service E-Learning</p>
            </div>
            <div style="padding:40px 32px;">
              <h2 style="color:#1A1208;">Bonjour %s,</h2>
              <p style="color:#3D2B0E;line-height:1.7;">
                Votre compte administrateur sur <strong>EMSI Certificate Generator</strong> vient d'être créé.
                Voici vos identifiants de connexion :
              </p>
              <div style="background:#F5EDD8;border-radius:8px;padding:20px;margin:24px 0;border:1px solid rgba(184,134,11,0.3);">
                <p style="margin:0;color:#7A5800;"><strong>👤 Nom d'utilisateur :</strong> %s</p>
                <p style="margin:8px 0 0;color:#7A5800;"><strong>🔑 Mot de passe temporaire :</strong> %s</p>
              </div>
              <p style="color:#B91C1C;font-size:13px;background:#FEF2F2;padding:10px;border-radius:6px;">
                ⚠️ Pour des raisons de sécurité, changez votre mot de passe dès votre première connexion.
              </p>
              <div style="text-align:center;margin:32px 0;">
                <a href="%s" style="background:linear-gradient(135deg,#7A5800,#B8860B);color:white;padding:14px 32px;border-radius:8px;text-decoration:none;font-weight:bold;font-size:16px;display:inline-block;">
                  🚀 Accéder à l'application
                </a>
              </div>
              <p style="color:#7A5C30;font-size:13px;text-align:center;">
                Lien direct : <a href="%s" style="color:#B8860B;">%s</a>
              </p>
            </div>
            <div style="background:#F5EDD8;padding:20px 32px;text-align:center;border-top:1px solid rgba(184,134,11,0.2);">
              <p style="color:#B89870;font-size:12px;margin:0;">© EMSI — École Marocaine des Sciences de l'Ingénieur</p>
            </div>
          </div>
        </body></html>
        """.formatted(name, username, pwd, loginLink, loginLink, loginLink);
    }

    private String buildWelcomeText(String name, String username, String pwd, String loginLink) {
        return "Bienvenue " + name + "!\n\n" +
               "Votre compte admin EMSI Certificate Generator a été créé.\n\n" +
               "Identifiants :\n" +
               "Username : " + username + "\n" +
               "Mot de passe : " + pwd + "\n\n" +
               "Accédez à l'application : " + loginLink + "\n\n" +
               "Changez votre mot de passe dès la première connexion.\n\n" +
               "EMSI — Service E-Learning";
    }
}
