package com.vsl700.nitflex.services.implementations;

import com.vsl700.nitflex.components.SharedProperties;
import com.vsl700.nitflex.components.WebsiteCredentials;
import com.vsl700.nitflex.exceptions.OutOfLuckException;
import com.vsl700.nitflex.exceptions.WebClientLoginException;
import com.vsl700.nitflex.services.MovieSeekerService;
import com.vsl700.nitflex.services.WebClientService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class MovieSeekerServiceImpl implements MovieSeekerService {
    private WebClientService webClientService;
    private WebsiteCredentials.Zamunda zamundaCredentials;
    private SharedProperties sharedProperties;

    private static final Logger LOG = LoggerFactory.getLogger(MovieSeekerServiceImpl.class);

    private static final String zamundaURL = "https://zamunda.net";
    private static final String zamundaCatalogURL = "https://zamunda.net/bananas";
    private static final String zamundaLoginPage = "https://zamunda.net/takelogin.php";

    @SneakyThrows
    @Override
    public URL findMovieURL() {
        // Login and get necessary cookie
        LOG.info("Logging in as '%s'...".formatted(zamundaCredentials.getUsername()));
        String cookie = webClientService.loginAndGetCookie(zamundaLoginPage,
                "username",
                "password",
                zamundaCredentials);
        if(!cookie.contains("uid"))
            throw new WebClientLoginException();

        LOG.info("Login successful!");

        // Load the Zamunda Top10 torrents table
        LOG.info("Looking for a movie to download...");
        String html = webClientService.getWebsiteContents(zamundaCatalogURL, cookie);
        Document doc = Jsoup.parse(html);
        var tableContentTableRows = doc.select("#div1 > table > tbody > tr").stream()
                .skip(1) // Skip the table's header
                .map(TableRow::new)
                .toList();

        // Filter out the table elements that don't meet the system's requirements
        List<TableRow> filtered = tableContentTableRows.stream()
                .filter(t -> t.typeOk) // Torrent type filter
                .filter(t -> sharedProperties.getMovieSizeLimit() == -1
                        || t.size < sharedProperties.getMovieSizeLimit()) // Size limit filter
                .filter(t -> !stringMatches(t.name, "S\\d+E\\d+")) // Single-episodes restriction filter
                .toList();

        // Pick a random torrent from the filtered table
        if(filtered.isEmpty())
            throw new OutOfLuckException("Could not pick a movie to download :( (html: %s)".formatted(html));

        TableRow chosenMovieTableRow = filtered.stream()
                .toList().get(new Random().nextInt(filtered.size()));

        // Pick the torrent that is the same as the already chosen one, but best quality possible
        String chosenMovieTorrentName = chosenMovieTableRow.name;
        chosenMovieTableRow = filtered.stream()
                .filter(t -> t.name.equals(chosenMovieTorrentName)) // Get equivalent torrents (table elements with the same names)
                .max((t1, t2) -> Float.compare(t1.size, t2.size)) // Get the torrent with the best quality (largest size)
                .orElseThrow();

        URL url = new URL(zamundaURL + chosenMovieTableRow.link);
        LOG.info("And we have a winner! ( %s )".formatted(url));

        // Return the torrent's review page link
        return url;
    }

    private boolean stringMatches(String text, String... regexes){
        for(String regex : regexes){
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text);

            if(matcher.find())
                return true;
        }

        return false;
    }

    private static class TableRow {
        public String name;
        public String link;
        public float size;
        public boolean typeOk;

        public TableRow(Element element){
            name = Objects.requireNonNull(element.selectFirst("td:nth-child(2) > a > b"))
                    .text();

            link = Objects.requireNonNull(element.selectFirst("td:nth-child(2) > a")).attr("href");

            String[] strs = Objects.requireNonNull(element.selectFirst("td:nth-child(4)"))
                    .text().split(" "); // i.e. "2.40 GB" will turn into {"2.40", "GB"},
                                              // and we'll take the number for the 'size'.
                                              // Also, if it's in MB we'll turn it into GB
            size = strs[1].equals("MB") ? Float.parseFloat(strs[0]) / 1024 : Float.parseFloat(strs[0]);

            String typeImgSrc = Objects.requireNonNull(element.selectFirst("td:nth-child(1) > img"))
                    .attr("src");

            typeOk = typeImgSrc.equals("https://zamunda.net/pic/img/pic/cat_movies_sd.gif") || typeImgSrc.equals("https://zamunda.net/pic/pic/cat_movies_sd.gif")
                    || typeImgSrc.equals("https://zamunda.net/pic/img/pic/cat_movs_hdtv.gif") || typeImgSrc.equals("https://zamunda.net/pic/pic/cat_movs_hdtv.gif")
                    || typeImgSrc.equals("https://zamunda.net/pic/img/pic/cat_movies_dvdr.gif") || typeImgSrc.equals("https://zamunda.net/pic/pic/cat_movies_dvdr.gif")
                    || typeImgSrc.equals("https://zamunda.net/pic/img/pic/cat_movies_xvidrus.gif") || typeImgSrc.equals("https://zamunda.net/pic/pic/cat_movies_xvidrus.gif")
                    || typeImgSrc.equals("https://zamunda.net/pic/img/pic/cat_movies_xvidbg.gif") || typeImgSrc.equals("https://zamunda.net/pic/pic/cat_movies_xvidbg.gif")
                    || typeImgSrc.equals("https://zamunda.net/pic/img/pic/cat_episodes_tveps.gif") || typeImgSrc.equals("https://zamunda.net/pic/pic/cat_episodes_tveps.gif")
                    || typeImgSrc.equals("https://zamunda.net/pic/img/pic/cat_episodes_tveps_hd.gif") || typeImgSrc.equals("https://zamunda.net/pic/pic/cat_episodes_tveps_hd.gif")
                    || typeImgSrc.equals("https://zamunda.net/pic/img/pic/cat_3d.gif") || typeImgSrc.equals("https://zamunda.net/pic/pic/cat_3d.gif")
                    || typeImgSrc.equals("https://zamunda.net/pic/img/pic/cat_movies_science.gif") || typeImgSrc.equals("https://zamunda.net/pic/pic/cat_movies_science.gif")
                    || typeImgSrc.equals("https://zamunda.net/pic/img/pic/cat_anime_anime.gif") || typeImgSrc.equals("https://zamunda.net/pic/pic/cat_anime_anime.gif")
                    || typeImgSrc.equals("https://zamunda.net/pic/img/pic/cat_bluray.gif") || typeImgSrc.equals("https://zamunda.net/pic/pic/cat_bluray.gif")
                    || typeImgSrc.equals("https://zamunda.net/pic/img/pic/cat_episodes_tveps_rus.gif") || typeImgSrc.equals("https://zamunda.net/pic/pic/cat_episodes_tveps_rus.gif");
        }
    }
}
