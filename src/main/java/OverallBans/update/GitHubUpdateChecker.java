package OverallBans.update;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GitHubUpdateChecker {

    private final String apiUrl;
    private final String currentVersion;

    public GitHubUpdateChecker(String apiUrl, String currentVersion) {
        this.apiUrl = apiUrl;
        this.currentVersion = currentVersion;
    }

    public UpdateInfo checkForUpdate() throws Exception {
        String response = httpGet(apiUrl);

        String latestVersion = extractStringField(response, "tag_name");
        String releaseUrl = extractStringField(response, "html_url");
        String downloadUrl = extractStringField(response, "browser_download_url");

        if (latestVersion == null || latestVersion.isEmpty()) {
            String errMsg = extractStringField(response, "message");
            throw new Exception("Could not parse tag_name from GitHub response. "
                    + (errMsg != null ? "GitHub message: " + errMsg : "Response length: " + response.length()));
        }

        return new UpdateInfo(latestVersion, currentVersion, downloadUrl, releaseUrl);
    }

    private String httpGet(String urlStr) throws IOException {
        HttpURLConnection con = null;
        try {
            URL url = new URL(urlStr);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/vnd.github.v3+json");
            con.setRequestProperty("User-Agent", "OverallBans-UpdateChecker");
            con.setConnectTimeout(15000);
            con.setReadTimeout(15000);

            int code = con.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ? con.getInputStream() : con.getErrorStream();
            if (is == null) is = con.getInputStream();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                return sb.toString();
            }
        } finally {
            if (con != null) con.disconnect();
        }
    }

    private String extractStringField(String json, String fieldName) {
        if (json == null) return null;
        String key = "\"" + fieldName + "\"";
        int idx = json.indexOf(key);
        if (idx < 0) return null;
        idx = json.indexOf(":", idx);
        if (idx < 0) return null;
        idx++;
        while (idx < json.length() && (json.charAt(idx) == ' ' || json.charAt(idx) == '\t')) idx++;
        if (idx >= json.length()) return null;
        if (json.charAt(idx) != '"') return null;
        idx++;
        int end = idx;
        StringBuilder sb = new StringBuilder();
        while (end < json.length()) {
            char c = json.charAt(end);
            if (c == '\\' && end + 1 < json.length()) {
                char next = json.charAt(end + 1);
                switch (next) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case '/': sb.append('/'); break;
                    default: sb.append(next); break;
                }
                end += 2;
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
                end++;
            }
        }
        return sb.toString();
    }
}
