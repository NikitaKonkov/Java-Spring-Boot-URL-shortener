package com.example.demo.url.Web;

import com.example.demo.url.Solutions.UrlCheck;
import com.example.demo.url.Url;
import com.example.demo.url.UrlController;
import com.example.demo.url.UrlRepository;
import com.example.demo.url.UrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/web")
public class UrlWebController {
    private final UrlRepository URLREPO;
    private final UrlWebService URLSERVICE;
    private static final Logger LOG = LoggerFactory.getLogger(UrlController.class);
    @Autowired
    public UrlWebController(UrlRepository URLREPO, UrlWebService URLSERVICE) {
        this.URLREPO = URLREPO;
        this.URLSERVICE = URLSERVICE;
    }

    @GetMapping("/home")
    public String homePage() {
        return "<!DOCTYPE html>"
               + "<html lang=\"en\">"
               + "<head>"
               + "    <meta charset=\"UTF-8\">"
               + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
               + "    <title>Home Page</title>"
               + "    <style>"
               + "        body {"
               + "            display: flex;"
               + "            flex-direction: column;"
               + "            align-items: center;"
               + "            justify-content: center;"
               + "            height: 100vh;"
               + "            text-align: center;"
               + "            margin: 0;"
               + "            font-family: 'Courier New', sans-serif;"
               + "            background-image: url(https://images.unsplash.com/photo-1617048551602-f377d4174089?q=80&w=3787&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D);"
               + "            background-size: cover;"
               + "            background-position: center;}h1,form,p {"
               + "            margin-bottom: 20px;"
               + "            background: rgba(255, 255, 255, 0.8);"
               + "            padding: 20px;"
               + "            border-radius: 10px;"
               + "        }"
               + "        form {"
               + "            display: flex;"
               + "            flex-direction: column;"
               + "            align-items: center;"
               + "        }"
               + "        input[type=\"text\"] {"
               + "            padding: 10px;"
               + "            font-size: 16px;"
               + "            margin-bottom: 10px;"
               + "        }"
               + "        input[type=\"submit\"] {"
               + "            padding: 10px 20px;"
               + "            font-size: 16px;"
               + "            cursor: pointer;"
               + "        }"
               + "        p {"
               + "            margin: 10px 0;"
               + "        }"
               + "        a {"
               + "            text-decoration: none;"
               + "            color: #0066cc;"
               + "        }"
               + "    </style>"
               + "</head>"
               + "<body>"
               + "    <h1>Welcome to the Home Page</h1>"
               + "    <form onsubmit=\"return submitForm()\">"
               + "        <input type=\"text\" id=\"id\" name=\"id\" placeholder=\"Enter ID\">"
               + "        <input type=\"submit\" value=\"Go to ID\">"
               + "    </form>"
               + "    <p><a href=\"/web/add\">Add a new URL</a></p>"
               + "    <p><a href=\"/web/urls\">View all URLs</a></p>"
               + "    <script>"
               + "        function submitForm() {"
               + "            var id = document.getElementById('id').value;"
               + "            window.location.href = '/web/' + id;"
               + "            return false;"
               + "        }"
               + "    </script>"
               + "</body>"
               + "</html>";
    }

    @GetMapping(path = "{id}")
    public String getUrlById(@PathVariable("id") String id) {
        String url = URLSERVICE.getUrlById(id);
        String remainingTtl = URLSERVICE.getRemainingTtlById(id);
        String html = String.format(
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "<title>URL</title>\n" +
                        "<style>\n" +
                        "body {\n" +
                        "  font-family: Courier New, sans-serif;\n" +
                        "  margin: 0;\n" +
                        "  padding: 0;\n" +
                        "  background-image: url('https://images.pexels.com/photos/7130540/pexels-photo-7130540.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1');\n" +
                        "  background-repeat: no-repeat;\n" +
                        "  background-attachment: fixed;\n" +
                        "  background-position: center;\n" +
                        "  background-size: cover;\n" +
                        "}\n" +
                        "h1, h3 {\n" +
                        "  color: #333;\n" +
                        "}\n" +
                        "body {\n" +
                        "  display: flex;\n" +
                        "  justify-content: center;\n" +
                        "  align-items: center;\n" +
                        "  height: 100vh;\n" +
                        "  text-align: center;\n" +
                        "}\n" +
                        ".box {\n" +
                        "  width: 450px;\n" +
                        "  height: 250px;\n" +
                        "  border: 1px solid black;\n" +
                        "  background-color: #fff;\n" +
                        "  padding: 10px;\n" +
                        "  margin: 10px;\n" +
                        "  box-shadow: 0 0 10px rgba(0,0,0,0.1);\n" +
                        "}\n" +
                        ".box h1, .box h3 {\n" +
                        "  text-align: center;\n" +
                        "}\n" +
                        "</style>\n" +
                        "<script>\n" +
                        "function updateTtl() {\n" +
                        "  var ttlElement = document.getElementById('ttl');\n" +
                        "  var remainingTtl = parseInt(ttlElement.innerHTML);\n" +
                        "  if (remainingTtl > 0) {\n" +
                        "    ttlElement.innerHTML = remainingTtl - 1;\n" +
                        "  } else {\n" +
                        "    clearInterval(intervalId);\n" +
                        "    ttlElement.innerHTML = 'NONE';\n" +
                        "  }\n" +
                        "}\n" +
                        "var intervalId = setInterval(updateTtl, 1000);\n" +
                        "</script>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "<div class='box'>\n" +
                        "  <h1>ABOUT THE URL!</h1>\n" +
                        "  <h3>URL [ %s ]</h3>\n" +
                        "  <h3>ID [ %s ]</h3>\n" +
                        "  <h3>REMAINING TTL [ <span id='ttl'>%s</span> ]</h3>\n" +
                        "  <h3>DATE[ %s ]</h3>\n" +
                        "  <button onclick=\"location.href='http://localhost:8080/web/home';\">Home</button>\n" +
                        "  <button onclick=\"location.href='http://localhost:8080/web/urls';\">URLs</button>\n" +
                        "</div>\n" +
                        "</body>\n" +
                        "</html>\n",
                url, id, remainingTtl, URLSERVICE.getDateById(id)
        );
        return html;
    }

    @GetMapping("/add")
    public String showForm() {
        return "<html>\n" +
                "<head>\n" +
                "<style>\n" +
                "body {\n" +
                "  text-align: center;\n" +
                "  background-image: url('https://images.pexels.com/photos/6985045/pexels-photo-6985045.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1');\n" +
                "  background-size: cover;\n" +
                "  height: 100vh;\n" +
                "  display: flex;\n" +
                "  justify-content: center;\n" +
                "  align-items: center;\n" +
                "}\n" +
                "form {\n" +
                "  font-family: 'Courier New', Courier, monospace;\n" +
                "  transform: scale(2);\n" +
                "}\n" +
                "form input[type='text'] {\n" +
                "  display: block;\n" +
                "  margin: 10px auto;\n" +
                "  padding: 10px;\n" +
                "  background-color: #FFFFFF;\n" +
                "  color: black;\n" +
                "  border: 3px solid black;\n" +
                "  border-radius: 4px;\n" +
                "}\n" +
                "form input[type='submit'], form input[type='button'] {\n" +
                "  display: block;\n" +
                "  margin: 20px auto;\n" +
                "  padding: 10px;\n" +
                "  background-color: #FFFFFF;\n" +
                "  color: black;\n" +
                "  border: 2px solid black;\n" +
                "  border-radius: 4px;\n" +
                "}\n" +
                "form label {\n" +
                "  font-weight: bold;\n" +
                "  margin-bottom: 10px;\n" +
                "}\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<form action='/web/add' method='post'>\n" +
                "  <label for='url'>URL</label><br>\n" +
                "  <input type='text' id='url' name='url'><br>\n" +
                "  <label for='ttl'>TTL</label><br>\n" +
                "  <input type='text' id='ttl' name='ttl'><br>\n" +
                "  <input type='submit' value='Submit'>\n" +
                "  <input type='button' onclick=\"location.href='http://localhost:8080/web/home';\" value='Go Home' />\n" +
                "</form>\n" +
                "</body>\n" +
                "</html>\n";}

    @PostMapping("/add")
    public ResponseEntity<Void> add(@RequestParam String url, @RequestParam Optional<Long> ttl) {
        Url newUrl = new Url();
        newUrl.setUrl(url);
        if (ttl.isPresent()) {
            newUrl.setTTL(ttl.get());
        }
        Url createdUrl = URLSERVICE.addNewUrl(newUrl);
        if (URLSERVICE.existsByUrl(url)) {
            URI location = URI.create("/web/" + URLSERVICE.getIdByUrl(url));
            return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
        }
        URI location = URI.create("/web/" + createdUrl.getID());
        return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
    }

    @GetMapping("/urls")
    public ResponseEntity<String> getUrls() {
        List<Url> urls = URLSERVICE.getUrls();
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            background-image: url('https://images.pexels.com/photos/6985045/pexels-photo-6985045.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1');\n" +
                "            background-repeat: no-repeat;\n" +
                "            background-size: cover;\n" +
                "            font-family: 'Courier New', Courier, monospace;\n" +
                "        }\n" +
                "        table {\n" +
                "            width: 100%;\n" +
                "            border-collapse: collapse;\n" +
                "        }\n" +
                "        th, td {\n" +
                "            border: 1px solid black;\n" +
                "            padding: 15px;\n" +
                "            text-align: left;\n" +
                "            background-color: white;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <table>\n" +
                "        <tr>\n" +
                "            <th>ID</th>\n" +
                "            <th>URL</th>\n" +
                "            <th>Remaining TTL</th>\n" +
                "            <th>Date</th>\n" +
                "        </tr>\n" +
                "        <!-- Add your table rows here -->\n" +
                "    </table>\n" +
                "</body>\n" +
                "</html>\n");
        for (Url url : urls) {
        String remainingTtl = url.getRemainingTtl() == null ? "<td style='background-color: green;'>STATIC</td>" : (url.getRemainingTtl() < 0 ? "<td style='background-color: red;'>" + url.getRemainingTtl() + "</td>" : "<td style='background-color: yellow;'>" + url.getRemainingTtl() + "</td>");
        html.append("<tr><td>").append(url.getID()).append("</td><td>").append(url.getUrl()).append("</td>").append(remainingTtl).append("<td>").append(url.getDATE()).append("</td></tr>");
        }
        html.append("</table></body></html>");
        return ResponseEntity.ok(html.toString());
    }
}