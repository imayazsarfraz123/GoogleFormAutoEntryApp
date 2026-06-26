package com.example.googleformautoentry;

import android.app.*;
import android.os.*;
import android.webkit.*;
import android.widget.*;
import android.view.*;
import android.graphics.Color;
import android.content.*;
import android.net.Uri;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    private static final int PICK_CSV = 10;
    private static final int PICK_FILE = 20;
    private WebView webView;
    private EditText urlBox, mappingBox;
    private TextView status;
    private ArrayList<Map<String,String>> rows = new ArrayList<>();
    private ArrayList<String> headers = new ArrayList<>();
    private int currentIndex = 0;
    private ValueCallback<Uri[]> filePathCallback;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        buildUi();
        configureWebView();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(12,12,12,12);
        setContentView(root);

        TextView title = new TextView(this);
        title.setText("Google Form Auto Entry v1.0");
        title.setTextSize(20); title.setTextColor(Color.BLACK); title.setPadding(0,0,0,8);
        root.addView(title);

        urlBox = new EditText(this);
        urlBox.setHint("Paste Google Form URL here");
        urlBox.setSingleLine(true);
        root.addView(urlBox, new LinearLayout.LayoutParams(-1, -2));

        mappingBox = new EditText(this);
        mappingBox.setHint("Mapping: Google Form Question=CSV Column\nExample:\nName=Full Name\nCNIC=CNIC");
        mappingBox.setMinLines(3);
        root.addView(mappingBox, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(buttons);
        addButton(buttons,"Load CSV", v -> pickCsv());
        addButton(buttons,"Open", v -> openForm());
        addButton(buttons,"Auto Fill", v -> autoFill());
        addButton(buttons,"Submit", v -> submitForm());

        LinearLayout buttons2 = new LinearLayout(this);
        buttons2.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(buttons2);
        addButton(buttons2,"Approve & Next", v -> nextRow("approved"));
        addButton(buttons2,"Reject", v -> nextRow("rejected"));
        addButton(buttons2,"Stop", v -> { currentIndex = 0; setStatus("Stopped."); });

        status = new TextView(this);
        status.setText("Ready. Load CSV, open form, then Auto Fill.");
        status.setTextColor(Color.DKGRAY); status.setPadding(0,8,0,8);
        root.addView(status);

        webView = new WebView(this);
        root.addView(webView, new LinearLayout.LayoutParams(-1,0,1));
    }

    private void addButton(LinearLayout parent, String text, View.OnClickListener l) {
        Button b = new Button(this); b.setText(text); b.setAllCaps(false); b.setOnClickListener(l);
        parent.addView(b, new LinearLayout.LayoutParams(0, -2, 1));
    }

    private void configureWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setLoadWithOverviewMode(true); s.setUseWideViewPort(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient(){
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> cb, FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = cb;
                Intent i = params.createIntent();
                try { startActivityForResult(i, PICK_FILE); } catch(Exception e) { filePathCallback = null; toast("No file picker found"); return false; }
                return true;
            }
        });
    }

    private void pickCsv() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("text/*");
        startActivityForResult(i, PICK_CSV);
    }

    private void openForm() {
        String u = urlBox.getText().toString().trim();
        if (u.isEmpty()) { toast("Enter Google Form URL"); return; }
        webView.loadUrl(u);
        setStatus("Form opened. Current row: " + (currentIndex+1) + "/" + rows.size());
    }

    private void autoFill() {
        if (rows.isEmpty()) { toast("Load CSV first"); return; }
        if (currentIndex >= rows.size()) { toast("All rows completed"); return; }
        Map<String,String> row = rows.get(currentIndex);
        Map<String,String> map = parseMapping(row);
        String js = buildFillJs(map);
        webView.evaluateJavascript(js, value -> setStatus("Auto-fill completed for row " + (currentIndex+1) + ". Review then Submit."));
    }

    private void submitForm() {
        String js = "(function(){var texts=['Submit','ارسال','جمع کریں','Send'];var btns=[...document.querySelectorAll('div[role=button],span,input[type=submit],button')];for(var b of btns){var t=(b.innerText||b.value||b.textContent||'').trim();if(texts.some(x=>t.toLowerCase().includes(x.toLowerCase()))){b.click();return 'clicked';}}return 'not found';})()";
        webView.evaluateJavascript(js, value -> setStatus("Submit command sent: " + value));
    }

    private void nextRow(String result) {
        if (rows.isEmpty()) { toast("Load CSV first"); return; }
        currentIndex++;
        if (currentIndex >= rows.size()) { setStatus("Completed. Total rows: " + rows.size()); toast("All rows completed"); return; }
        setStatus("Marked previous row " + result + ". Next row: " + (currentIndex+1) + "/" + rows.size());
        openForm();
        new Handler().postDelayed(this::autoFill, 2500);
    }

    private Map<String,String> parseMapping(Map<String,String> row) {
        LinkedHashMap<String,String> out = new LinkedHashMap<>();
        String txt = mappingBox.getText().toString();
        for (String line : txt.split("\\n")) {
            if (!line.contains("=")) continue;
            String[] p = line.split("=",2);
            String question = p[0].trim(); String col = p[1].trim();
            if (!question.isEmpty() && row.containsKey(col)) out.put(question, row.get(col));
        }
        if (out.isEmpty()) for (String h: headers) out.put(h, row.get(h));
        return out;
    }

    private String esc(String s) { return s == null ? "" : s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", " ").replace("\r", " "); }

    private String buildFillJs(Map<String,String> mapping) {
        StringBuilder data = new StringBuilder("{");
        int i=0; for (Map.Entry<String,String> e: mapping.entrySet()) { if(i++>0)data.append(','); data.append("'").append(esc(e.getKey())).append("':'").append(esc(e.getValue())).append("'"); }
        data.append("}");
        return "(function(){var data="+data+";function ev(el){el.dispatchEvent(new Event('input',{bubbles:true}));el.dispatchEvent(new Event('change',{bubbles:true}));}function norm(s){return (s||'').toLowerCase().replace(/\\s+/g,' ').trim();}var filled=0;for(var q in data){var val=data[q];var qn=norm(q);var items=[...document.querySelectorAll('div[role=listitem],.Qr7Oae,div.freebirdFormviewerViewNumberedItemContainer')];var item=items.find(x=>norm(x.innerText||x.textContent).includes(qn));if(!item){var el=[...document.querySelectorAll('input,textarea')].find(x=>norm(x.getAttribute('aria-label')||x.placeholder||x.name||x.id).includes(qn));if(el){el.value=val;ev(el);filled++;continue;}}if(item){var input=item.querySelector('textarea,input[type=text],input[type=email],input[type=number],input[type=tel],input:not([type])');if(input){input.focus();input.value=val;ev(input);filled++;continue;}var choices=[...item.querySelectorAll('div[role=radio],div[role=checkbox],label,span')];var c=choices.find(x=>norm(x.innerText||x.textContent).includes(norm(val)));if(c){c.click();filled++;continue;}}}return 'filled '+filled;})()";
    }

    @Override protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req,res,data);
        if (req == PICK_CSV && res == RESULT_OK && data != null) readCsv(data.getData());
        if (req == PICK_FILE) {
            if (filePathCallback == null) return;
            Uri[] uris = null;
            if (res == RESULT_OK && data != null) uris = WebChromeClient.FileChooserParams.parseResult(res, data);
            filePathCallback.onReceiveValue(uris); filePathCallback = null;
        }
    }

    private void readCsv(Uri uri) {
        try {
            rows.clear(); headers.clear(); currentIndex = 0;
            BufferedReader br = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));
            String line; ArrayList<String[]> raw = new ArrayList<>();
            while((line=br.readLine())!=null) raw.add(parseCsvLine(line)); br.close();
            if (raw.isEmpty()) { toast("CSV is empty"); return; }
            headers.addAll(Arrays.asList(raw.get(0)));
            for(int r=1;r<raw.size();r++) { String[] vals=raw.get(r); LinkedHashMap<String,String> m=new LinkedHashMap<>(); for(int c=0;c<headers.size();c++) m.put(headers.get(c), c<vals.length?vals[c]:""); rows.add(m); }
            setStatus("CSV loaded. Rows: "+rows.size()+". Columns: "+headers.size());
            toast("CSV loaded successfully");
        } catch(Exception e) { toast("CSV error: "+e.getMessage()); setStatus("CSV error: "+e.getMessage()); }
    }

    private String[] parseCsvLine(String line) {
        ArrayList<String> out = new ArrayList<>(); StringBuilder sb = new StringBuilder(); boolean q=false;
        for(int i=0;i<line.length();i++){ char ch=line.charAt(i); if(ch=='\"'){ if(q && i+1<line.length() && line.charAt(i+1)=='\"'){sb.append('"'); i++;} else q=!q; } else if(ch==',' && !q){ out.add(sb.toString().trim()); sb.setLength(0);} else sb.append(ch); }
        out.add(sb.toString().trim()); return out.toArray(new String[0]);
    }

    private void setStatus(String s) { status.setText(s); }
    private void toast(String s) { Toast.makeText(this,s,Toast.LENGTH_LONG).show(); }
}
