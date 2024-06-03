package com.vsl700.nitflex;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AlgorithmTests {

    @Test
    public void matcher_Test(){
        String seasonEpisode = "S02E109";

        String matcher = getMatcher("S\\d\\dE\\d\\d\\d", "Wednesday.%s.BRRip".formatted(seasonEpisode));

        Assertions.assertEquals(seasonEpisode, matcher);
    }

    @Test
    public void episodeRegex_Test(){
        int matches = regexMatches("S[0-9][0-9]E[0-9][0-9]", "Wednesday.S01E02.BRRip");

        Assertions.assertEquals(1, matches);
    }

    @Test
    public void episodeRegex_Test2(){
        int matches = regexMatches("S[0-9][0-9]E[0-9][0-9][0-9]", "Wednesday.S01E02.BRRip");

        Assertions.assertEquals(0, matches);
    }

    @Test
    public void episodeRegex_Test3(){
        int matches = regexMatches("S\\d\\dE\\d\\d\\d", "Wednesday.S02E109.BRRip");

        Assertions.assertEquals(1, matches);
    }

    @Test
    public void episodeRegex_Test4(){
        String seasonEpisode = "S02E109";

        int matches_4digits = regexMatches("S\\d\\dE\\d\\d\\d\\d", "Wednesday.%s.BRRip".formatted(seasonEpisode));
        int matches_3digits = regexMatches("S\\d\\dE\\d\\d\\d", "Wednesday.%s.BRRip".formatted(seasonEpisode));
        int matches_2digits = regexMatches("S\\d\\dE\\d\\d", "Wednesday.%s.BRRip".formatted(seasonEpisode));

        Assertions.assertEquals(1, Math.max(matches_4digits, Math.max(matches_3digits, matches_2digits)));
    }

    @Test
    public void episodeRegex_Test5(){
        String seasonEpisode = "S102E109";

        int matches_4digits = regexMatches("S[0-9]+E\\d\\d\\d\\d", "Wednesday.%s.BRRip".formatted(seasonEpisode));
        int matches_3digits = regexMatches("S[0-9]+E\\d\\d\\d", "Wednesday.%s.BRRip".formatted(seasonEpisode));
        int matches_2digits = regexMatches("S[0-9]+E\\d\\d", "Wednesday.%s.BRRip".formatted(seasonEpisode));

        Assertions.assertEquals(1, Math.max(matches_4digits, Math.max(matches_3digits, matches_2digits)));
    }

    @Test
    public void episodeRegex_Test6(){
        String seasonEpisode1 = "fdsafsda S102E109";
        String seasonEpisode2 = "fdsaS02E10";
        String seasonEpisode3 = "fdsaaS02E1004";

        String regex = "S\\d+E\\d+"; // '\\d' instead of '[0-9]' also works!

        int matches1 = regexMatches(regex, seasonEpisode1);
        int matches2 = regexMatches(regex, seasonEpisode2);
        int matches3 = regexMatches(regex, seasonEpisode3);

        Assertions.assertAll(() -> {
            Assertions.assertEquals(1, matches1);
            Assertions.assertEquals(1, matches2);
            Assertions.assertEquals(1, matches3);
        });
    }

    @Test
    public void episodeRegex_String_matches_Test(){
        String seasonEpisode1 = "fdsaf S102E109";
        String seasonEpisode2 = "fdsS02E10";
        String seasonEpisode3 = "fdsaS02E1004";

        String regex = "S\\d+E\\d+"; // '\\d' instead of '[0-9]' also works!

        boolean matches1 = seasonEpisode1.matches(regex);
        boolean matches2 = seasonEpisode2.matches(regex);
        boolean matches3 = seasonEpisode3.matches(regex);

        Assertions.assertAll(() -> {
            Assertions.assertFalse(matches1);
            Assertions.assertFalse(matches2);
            Assertions.assertFalse(matches3);
        });
    }

    private String getMatcher(String regex, String text){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        return matcher.find() ? matcher.group() : "";
    }

    private int regexMatches(String regex, String text){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        int matches = 0;
        while (matcher.find()) {
            matches++;
        }

        return matches;
    }

    @Test
    public void stringComparing_differentCharsets_Test(){
        String utf8Str = new String("привет".getBytes(), StandardCharsets.UTF_8);
        String win1251Str = new String("привет".getBytes(Charset.forName("windows-1251")), Charset.forName("windows-1251"));

        assertEquals(utf8Str, win1251Str);
    }

}
