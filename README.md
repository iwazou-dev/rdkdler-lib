# rdkdler-lib

`rdkdler-lib` は、日本のラジオ配信サービス「Radiko」のタイムフリー番組をJavaから簡単にダウンロードするためのライブラリです。

> ⚠️ 注意  
> 本ライブラリは非公式です。radiko の提供元とは関係ありません。  
> 利用にあたっては各サービスの利用規約・著作権等を遵守してください。

## 特徴

* Radikoタイムフリー番組の取得・保存
* 番組情報の検索機能
* 番組表の取得

## 動作環境

開発・動作確認は以下の環境で行っています。

| 項目 | 推奨バージョン |
| --- | --- |
| **JDK** | Temurin 21.0.9 以上 |
| **Gradle** | 9.2.1 以上 |
| **FFmpeg** | 7.1.1_4 以上 |

## 事前準備

本ライブラリは、音声データのエンコードおよび結合に **FFmpeg** を使用します。
ご利用の環境に合わせて、事前にFFmpegをインストールし、パスを通しておいてください。


## 利用方法

本ライブラリはJitPack(https://jitpack.io/#iwazou-dev/rdkdler-lib)で公開しています。リンク先にもありますが以下の設定を行うことで利用出来ます。（Gradleの場合）

`settings.gradle.kts`に以下を追加。
```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```
`dependencies`に以下を追加。
```kotlin
dependencies {
    // タイムフリー番組の取得
    implementation("com.github.iwazou-dev.rdkdler-lib:rdkdler-download:1.0.0")
    // 番組情報の検索・番組表の取得
    implementation("com.github.iwazou-dev.rdkdler-lib:rdkdler-program:1.0.0")
}
```

### タイムフリー番組の保存

```java
package org.example;

import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.LocalDateTime;

import net.iwazou.rdkdler.download.DefaultFFmpegFactory;
import net.iwazou.rdkdler.download.RdkAuthenticator;
import net.iwazou.rdkdler.download.RdkDownloadService;
import net.iwazou.rdkdler.http.RdkHttpClient;
import net.iwazou.rdkdler.http.internal.JdkRdkHttpClient;

public class DownloadExample {
    public static void main(String[] args) throws Exception {
        // HTTPクライアント（JDK標準 HttpClient をラップ）
        RdkHttpClient client = new JdkRdkHttpClient(HttpClient.newHttpClient());

        // 認証 + FFmpegFactory + ダウンロードサービス
        RdkAuthenticator authenticator = new RdkAuthenticator(client);
        RdkDownloadService service = new RdkDownloadService(authenticator, new DefaultFFmpegFactory());

        // 例: stationId / 放送開始-終了（タイムフリーの範囲）
        String stationId = "TBS";
        LocalDateTime from = LocalDateTime.of(2026, 1, 10, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 1, 10, 2, 0, 0);

        Path out = Path.of("out.m4a");

        // ダウンロード
        service.download(stationId, from, to, out);
    }
}
```

### 番組の検索

```java
package org.example;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.format.DateTimeFormatter;

import net.iwazou.rdkdler.area.AreaPrefecture;
import net.iwazou.rdkdler.area.AreaPrefectureService;
import net.iwazou.rdkdler.http.RdkHttpClient;
import net.iwazou.rdkdler.http.internal.JdkRdkHttpClient;
import net.iwazou.rdkdler.model.ProgramSearchResult;
import net.iwazou.rdkdler.model.ProgramSearchResult.ResultData;
import net.iwazou.rdkdler.search.ProgramSearchService;

public class SearchExample {
    public static void main(String[] args) throws IOException, InterruptedException {
        RdkHttpClient client = new JdkRdkHttpClient(HttpClient.newHttpClient());
        // 現在地のエリア
        AreaPrefectureService areaService = new AreaPrefectureService(client);
        AreaPrefecture area = areaService.getCurrentAreaPrefecture();
        System.out.printf("現在地エリア : %s(%s)\n", area.getAreaId(), area.getKanjiName());

        // 番組の検索
        ProgramSearchService searchService = new ProgramSearchService(client, area);
        int pageIndex = 0;
        ProgramSearchResult result = searchService.search("ニュース", pageIndex);

        System.out.printf("全件数 : %s\n", result.getMeta().getResultCount());
        System.out.printf("１ページの件数 : %s\n", result.getMeta().getRowLimit());
        System.out.printf("ページインデックス : %s\n", result.getMeta().getPageIdx());
        DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (ResultData data : result.getResultDatas()) {
            // 放送局ID、開始時間、終了時間、タイトル
            System.out.printf("[%-10s] %s - %s %s\n",
                    data.getStationId(),
                    data.getStartTime().format(FMT),
                    data.getEndTime().format(FMT),
                    data.getTitle());
        }
    }
}
```

### 番組表の取得

```java
package org.example;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.LocalDate;

import net.iwazou.rdkdler.area.AreaPrefecture;
import net.iwazou.rdkdler.area.AreaPrefectureService;
import net.iwazou.rdkdler.http.RdkHttpClient;
import net.iwazou.rdkdler.http.internal.JdkRdkHttpClient;
import net.iwazou.rdkdler.model.DailyProgramSchedule;
import net.iwazou.rdkdler.model.ProgramEntry;
import net.iwazou.rdkdler.model.ProgramSchedule;
import net.iwazou.rdkdler.model.StationProgramSchedule;
import net.iwazou.rdkdler.schedule.ProgramScheduleService;

public class GetProgramExample {
    public static void main(String[] args) throws IOException, InterruptedException {
        RdkHttpClient client = new JdkRdkHttpClient(HttpClient.newHttpClient());
        // 現在地のエリア
        AreaPrefectureService areaService = new AreaPrefectureService(client);
        AreaPrefecture area = areaService.getCurrentAreaPrefecture();
        System.out.printf("現在地エリア : %s(%s)\n", area.getAreaId(), area.getKanjiName());

        ProgramScheduleService pssvc = new ProgramScheduleService(client);
        // 特定のエリアの特定の日付の番組情報
        ProgramSchedule schedule = pssvc.getProgramSchedule(area, LocalDate.now().minusDays(1));
        // 特定の放送局の数週間分の番組情報
        // ProgramSchedule schedule = pssvc.getProgramSchedule("TBS");

        for (StationProgramSchedule sch : schedule.getStationProgramSchedules()) {
            System.out.printf("放送局ID : %s, 放送局名 : %s\n", sch.getStationId(), sch.getStationName());
            for (DailyProgramSchedule daily : sch.getDailyProgramSchedules()) {
                for (ProgramEntry entry : daily.getProgramEntrys()) {
                    // 開始時間、終了時間、タイトル
                    System.out.printf("  %s - %s %s\n", entry.getFt(), entry.getTo(), entry.getTitle());
                }
            }
        }
    }
}
```


## リンク・連絡先

開発者：**いわぞう**

* [Bluesky](https://bsky.app/profile/iwazou.bsky.social)
* [Zenn](https://zenn.dev/iwazou)
* [X (Twitter)](https://x.com/iwazou)

## 謝辞・参考

本ライブラリの開発にあたり、以下のプロジェクトを参考にさせていただきました。素晴らしい知見の共有に感謝いたします。

* [uru2/rec_radiko_ts](https://github.com/uru2/rec_radiko_ts)

## ライセンス

[MIT License](https://www.google.com/search?q=LICENSE)

