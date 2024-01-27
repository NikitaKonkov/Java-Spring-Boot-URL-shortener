package com.example.demo.url.Web;

import com.example.demo.url.Exceptions.WarningException;
import com.example.demo.url.Exceptions.WebPageException;
import com.example.demo.url.Solutions.UrlCheck;
import com.example.demo.url.Solutions.UrlHash;
import com.example.demo.url.Url;
import com.example.demo.url.UrlRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class UrlWebService {
    private static final Logger LOG = LoggerFactory.getLogger(UrlWebService.class);
    private final UrlRepository URLREPO;
    private final ScheduledExecutorService EXECSERVICE;
    @Autowired
    public UrlWebService(UrlRepository URLREPO) {
        this.URLREPO = URLREPO;
        this.EXECSERVICE = Executors.newScheduledThreadPool(1);
    }
    private String checkUrlValidity(String url) {
        return UrlCheck.check(url) ? "VALID" : "INVALID";
    }
    List<Url> getUrls() {
        return new ArrayList<>(URLREPO.findAll());
    }
    ConcurrentHashMap<String, Long> URLTTLS = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, Future<?>> URLTASKS = new ConcurrentHashMap<>();
    public Url addNewUrl(Url url) {
        String valid = checkUrlValidity(url.getUrl());

        if (url.getID() != null && URLREPO.findById(url.getID()).isPresent()) {
            throw new WarningException("ID " + url.getID() + " ALREADY REGISTERED BY "+url.getUrl());
        }
        if (url.getID() == null) {
            url.setID(UrlHash.hashTime());
        }
        if (URLREPO.existsByUrl(url.getUrl())) {
            LOG.error("URL " + url.getUrl() + " WITH ID " + getIdByUrl(url.getUrl()) + " ALREADY REGISTERED");
            throw new WebPageException(
                      "<!DOCTYPE html>\n" +
                              "<html lang=\"en\">\n" +
                              "\n" +
                              "<head>\n" +
                              "    <meta charset=\"UTF-8\">\n" +
                              "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                              "    <title>URL Already Registered</title>\n" +
                              "    <style>\n" +
                              "        body {\n" +
                              "            display: flex;\n" +
                              "            align-items: center;\n" +
                              "            justify-content: center;\n" +
                              "            height: 100vh;\n" +
                              "            margin: 0;\n" +
                              "            background-image: url(https://images.unsplash.com/photo-1554990349-170b9e4bdf3b?q=80&w=4040&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D);\n" +
                              "            background-size: cover;\n" +
                              "            background-position: center;\n" +
                              "            color: #000;\n" +
                              "            font-family: 'Courier New', sans-serif;\n" +
                              "        }\n" +
                              "        .error-box {\n" +
                              "            padding: 100px;\n" +
                              "            border: 5px solid #000;\n" +
                              "            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);\n" +
                              "            text-align: center;\n" +
                              "            background-color: rgba(255, 255, 255, 0.8);\n" +
                              "            font-size: 24px; /* Adjust the font size as needed */\n" +
                              "            font-weight: bold; /* Make the text bold */\n" +
                              "        }\n" +
                              "        .link-button {\n" +
                              "            display: inline-block;\n" +
                              "            padding: 10px 20px;\n" +
                              "            margin-top: 20px;\n" +
                              "            background-color: #0066cc;\n" +
                              "            color: #fff;\n" +
                              "            text-decoration: none;\n" +
                              "            border-radius: 5px;\n" +
                              "        }\n" +
                              "    </style>\n" +
                              "</head>\n" +
                              "<body>\n" +
                              "    <div class=\"error-box\">\n" +
                              "        <p>URL "+url.getUrl()+" Already Registered with ID "+getIdByUrl(url.getUrl())+"</p>\n" +
                              "        <a href=\"http://localhost:8080/web/"+getIdByUrl(url.getUrl())+"\" class=\"link-button\">Go to URL Page</a>\n" +
                              "    </div>\n" +
                              "</body>\n" +
                              "</html>\n");
        }
        Url savedUrl = URLREPO.save(url);
        Long ttl = url.getTTL();
        handleTtl(savedUrl, ttl, valid);
        return savedUrl;
    }
    private void handleTtl(Url savedUrl, Long ttl, String valid) {
        if (ttl != null) {
            if (ttl < 0) {
                ttl = Math.abs(ttl);
                LOG.warn("NEGATIVE TTL DETECTED, TTL INVERTED, "+valid+" URL {} WITH ID {} CREATED", savedUrl.getUrl(), savedUrl.getID());}
            else if (ttl == 0) {
                ttl = 1L;
                LOG.warn("ZERO TTL DETECTED, "+valid+" URL {} WILL BE DELETED, WITH ID {}", savedUrl.getUrl(), savedUrl.getID());
            }
            if (ttl > 0) {
                URLTTLS.put(savedUrl.getID(), ttl);
                Future<?> task = EXECSERVICE.scheduleAtFixedRate(() -> {
                    long currentTTL = URLTTLS.get(savedUrl.getID());
                    if (currentTTL > 0) {
                        URLTTLS.put(savedUrl.getID(), currentTTL - 1);
                    } else if (currentTTL == 0) {
                        LOG.info("URL "+getUrlById(savedUrl.getID())+" WITH ID "+savedUrl.getID()+" DELETED");
                        URLREPO.deleteById(savedUrl.getID());
                        if (URLTASKS.containsKey(savedUrl.getID())) {
                            URLTASKS.get(savedUrl.getID()).cancel(false);
                            URLTASKS.remove(savedUrl.getID());
                        }
                    }
                }, 1, 1, TimeUnit.SECONDS);
                URLTASKS.put(savedUrl.getID(), task);
            }
        } else {
            LOG.warn("NO TTL DETECTED, "+valid+" URL {} WITH ID {} STAY FOREVER", savedUrl.getUrl(), savedUrl.getID());
        }
    }
    public String getIdByUrl(String url) {
        return URLREPO.findIdByUrl(url)
                .orElseThrow(() -> new EntityNotFoundException("URL " + url + " NOT FOUND"));
    }
    public String getUrlById(String id) {
        return URLREPO.findById(id)
                .map(Url::getUrl)
                .orElseThrow(() -> {
                    LOG.error("URL WITH ID {} NOT FOUND OR WAS DELETED", id);
                    return new WebPageException("<!DOCTYPE html>\n" +
                            "<html lang=\"en\">\n" +
                            "<head>\n" +
                            "    <meta charset=\"UTF-8\">\n" +
                            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                            "    <title>URL NOT FOUND</title>\n" +
                            "    <style>\n" +
                            "        body {\n" +
                            "            display: flex;\n" +
                            "            align-items: center;\n" +
                            "            justify-content: center;\n" +
                            "            height: 100vh;\n" +
                            "            margin: 0;\n" +
                            "            background-image: url(https://images.unsplash.com/photo-1530950837622-262e7f56f087?q=80&w=2832&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D);\n" +
                            "            background-size: cover;\n" +
                            "            background-position: center;\n" +
                            "            color: #000;\n" +
                            "            font-family: 'Courier New', sans-serif;\n" +
                            "        }\n" +
                            "        .error-box {\n" +
                            "            padding: 100px;\n" +
                            "            border: 5px solid #000;\n" +
                            "            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);\n" +
                            "            text-align: center;\n" +
                            "            background-color: rgba(255, 255, 255, 0.8);\n" +
                            "            font-size: 24px; /* Adjust the font size as needed */\n" +
                            "            font-weight: bold; /* Make the text bold */\n" +
                            "        }\n" +
                            "        .add-url-button {\n" +
                            "            display: inline-block;\n" +
                            "            padding: 10px 20px;\n" +
                            "            margin-top: 20px;\n" +
                            "            background-color: #0066cc;\n" +
                            "            color: #fff;\n" +
                            "            text-decoration: none;\n" +
                            "            border-radius: 5px;\n" +
                            "        }\n" +
                            "    </style>\n" +
                            "</head>\n" +
                            "<body>\n" +
                            "    <div class=\"error-box\">\n" +
                            "        <p>URL WITH ID <?= id ?> NOT FOUND OR WAS DELETED</p>\n" +
                            "        <a href=\"http://localhost:8080/web/add\" class=\"add-url-button\">Add New URL</a>\n" +
                            "    </div>\n" +
                            "</body>\n" +
                            "</html>\n");
                });
    }
    public LocalDateTime getDateById(String id){
        return URLREPO.findById(id)
                .map(Url::getDATE)
                .orElseThrow(() -> {
                    LOG.error("URL WITH ID {} NOT FOUND OR WAS DELETED", id);
                    return new WebPageException("<!DOCTYPE html>\n" +
                            "<html lang=\"en\">\n" +
                            "<head>\n" +
                            "    <meta charset=\"UTF-8\">\n" +
                            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                            "    <title>URL NOT FOUND</title>\n" +
                            "    <style>\n" +
                            "        body {\n" +
                            "            display: flex;\n" +
                            "            align-items: center;\n" +
                            "            justify-content: center;\n" +
                            "            height: 100vh;\n" +
                            "            margin: 0;\n" +
                            "            background-image: url(https://images.unsplash.com/photo-1530950837622-262e7f56f087?q=80&w=2832&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D);\n" +
                            "            background-size: cover;\n" +
                            "            background-position: center;\n" +
                            "            color: #000;\n" +
                            "            font-family: 'Courier New', sans-serif;\n" +
                            "        }\n" +
                            "        .error-box {\n" +
                            "            padding: 100px;\n" +
                            "            border: 5px solid #000;\n" +
                            "            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);\n" +
                            "            text-align: center;\n" +
                            "            background-color: rgba(255, 255, 255, 0.8);\n" +
                            "            font-size: 24px; /* Adjust the font size as needed */\n" +
                            "            font-weight: bold; /* Make the text bold */\n" +
                            "        }\n" +
                            "        .add-url-button {\n" +
                            "            display: inline-block;\n" +
                            "            padding: 10px 20px;\n" +
                            "            margin-top: 20px;\n" +
                            "            background-color: #0066cc;\n" +
                            "            color: #fff;\n" +
                            "            text-decoration: none;\n" +
                            "            border-radius: 5px;\n" +
                            "        }\n" +
                            "    </style>\n" +
                            "</head>\n" +
                            "<body>\n" +
                            "    <div class=\"error-box\">\n" +
                            "        <p>URL WITH ID <?= id ?> NOT FOUND OR WAS DELETED</p>\n" +
                            "        <a href=\"http://localhost:8080/web/add\" class=\"add-url-button\">Add New URL</a>\n" +
                            "    </div>\n" +
                            "</body>\n" +
                            "</html>\n");

                });
    }
    @Transactional
    public void updateUrl(String id, String newUrl) {
        Url existingUrl = URLREPO.findById(id).orElseThrow(() -> new EntityNotFoundException("URL WITH ID "+id+" NOT FOUND"));
        LOG.info("URL {} WITH ID {} CHANGED TO {}",getUrlById(id),id,newUrl);
        existingUrl.setUrl(newUrl);
        URLREPO.save(existingUrl);
    }

    public boolean existsByUrl(String url) {
        return URLREPO.findIdByUrl(url).isPresent();
    }
    public String getRemainingTtlById(String id) {
        return String.valueOf(URLREPO.findById(id).get().getRemainingTtl()); // It works I don't care
    }
}
