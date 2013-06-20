package uguu.gao.wafu.NewsDeRenshuu;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.atilika.kuromoji.Token;
import org.atilika.kuromoji.Tokenizer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        CardGeneratorTask cardGeneratorTask = new CardGeneratorTask();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            cardGeneratorTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
        else
            cardGeneratorTask.execute((Void[])null);

    }

    private class CardGeneratorTask extends AsyncTask<Void, Void, Void> {
        String res;
        ArrayList<String> headings;
        ArrayList<Cards> cards;
        protected Void doInBackground(Void... v) {
            try {
                String siteData = getWebsite("http://www.yomiuri.co.jp/latestnews");
                Document doc = Jsoup.parse(siteData);
                headings = new ArrayList<String>();
                for (Element links : doc.select("ul.list-def")) {
                    for (Element link : links.select("li")) {
                        generateCards(link.text());
                    }
                }
                res = siteData;

            } catch (Exception e) {

            }
            // download each page
            // parse each headline into question answer pairs with link to article
            // write pairs to database
            return null;
        }

        private void generateCards(String heading) {
            Tokenizer tokenizer = Tokenizer.builder().build();
            List<Token> tokens = tokenizer.tokenize(heading);
            for (Token token : tokens) {
                String partOfSpeech = token.getPartOfSpeech();
                String[] parts = partOfSpeech.split(",");
                if (!parts[0].equals("記号")) // no symbols
                if (!parts[0].equals("助詞")) // no particles
                if (!(parts[0].equals("名詞") && parts[1].equals("数"))) { // no numbers
                    if (token.getReading() != null) {
                        cards.add(new Cards(heading, token.getSurfaceForm(), token.getReading()));
                    }
                }
            }
        }

        private String getWebsite(String site) throws IOException {
            InputStream is = null;
            String content = null;
            try {
                URL url = new URL(site);
                URLConnection urlc = url.openConnection();
                BufferedReader buf = new BufferedReader(new InputStreamReader(urlc.getInputStream(), "SHIFT_JIS"));
                StringBuilder sb = new StringBuilder();
                String str = "";
                while ((str = buf.readLine()) != null) {
                    sb.append(str);
                }
                content = sb.toString();

            } catch (Exception e) {

            }
            return content;
        }

        protected void onPostExecute(Void v) {
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.INVISIBLE);
            TextView loadingText = (TextView) findViewById(R.id.textView4);
            loadingText.setVisibility(View.INVISIBLE);
            TextView answerLabel = (TextView) findViewById(R.id.textView3);
            EditText answerEditText = (EditText) findViewById(R.id.editText);
            Button checkButton = (Button) findViewById(R.id.button);
            Button nextButton = (Button) findViewById(R.id.button2);

            answerEditText.setVisibility(View.VISIBLE);
            answerLabel.setVisibility(View.VISIBLE);
            checkButton.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.VISIBLE);

            // set first card

            TextView headingText = (TextView) findViewById(R.id.textView);
            TextView kanjiText = (TextView) findViewById(R.id.textView2);
            Cards card = cards.get(0);

            headingText.setText(card.heading);
            kanjiText.setText(card.kanji);

        }
    }
}
