package org.monroe.team.toolsbox.us;

import org.junit.Ignore;
import org.junit.Test;
import org.monroe.team.toolsbox.logging.Logs;

import static org.junit.Assert.*;

public class ExploreDownloadUrlTest {

    @Ignore
    @Test public void encodingTesting() throws ExploreDownloadUrlDefinition.UnreachableUrlException {
        ExploreDownloadUrl exploreDownloadUrl = new ExploreDownloadUrl();
        exploreDownloadUrl.log = Logs.core;

        String url = "http://r6---sn-aigllnsz.googlevideo.com/videoplayback?id=o-ALSV_6BhKoY9qoPwJ-Z-n54cti01ngqF5sr2hQWft6Qo&initcwndbps=2469000&ms=au&mt=1405537959&signature=F17674825E3512353B0E36312CC45B093C8C29F1.8FBBCC0DCF88E1AEEAD430DB800E37678B3059AF&key=yt5&source=youtube&ratebypass=yes&expire=1405562400&mws=yes&sver=3&ipbits=0&mv=m&nh=IgpwcjAyLmxocjE0KgkxMjcuMC4wLjE&ip=2a02%3A2498%3Ae002%3A88%3A225%3A90ff%3Afe7c%3Ab806&itag=22&upn=Cz_fWLw___A&fexp=902408%2C924213%2C924217%2C924222%2C930008%2C931330%2C934024%2C934030%2C935019%2C944314%2C945035&sparams=id%2Cinitcwndbps%2Cip%2Cipbits%2Citag%2Cratebypass%2Csource%2Cupn%2Cexpire&title=UXD-+It%27s+all+about+location%2C+Part+3-3+Design+for+the+real+world";
        System.out.println(exploreDownloadUrl.perform(url).downloadUrlDetails.fileName);
    }

}