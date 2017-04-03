package com.james.status.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.async.Action;
import com.james.status.R;
import com.james.status.adapters.FaqAdapter;
import com.james.status.data.FaqData;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HelpFragment extends SimpleFragment {

    private static final String
            FAQ_URL = "https://theandroidmaster.github.io/apps/status/faq.json",
            COMMUNITY_URL = "https://plus.google.com/communities/100021389226995148571";

    private RecyclerView recycler;
    private ProgressBar progressBar;
    private FaqAdapter adapter;
    private View emptyView;
    private List<FaqData> faqs;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_help, container, false);

        recycler = (RecyclerView) v.findViewById(R.id.recycler);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        emptyView = v.findViewById(R.id.empty);

        recycler.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recycler.setNestedScrollingEnabled(false);
        faqs = new ArrayList<>();

        new Action<String>() {
            @NonNull
            @Override
            public String id() {
                return "faqs";
            }

            @Nullable
            @Override
            protected String run() throws InterruptedException {
                try {
                    URL url = new URL(FAQ_URL);
                    HttpURLConnection request = (HttpURLConnection) url.openConnection();
                    request.connect();

                    JsonReader reader = new JsonReader(new InputStreamReader((InputStream) request.getContent(), "UTF-8"));

                    reader.beginArray();
                    while (reader.hasNext()) {
                        reader.beginObject();

                        String name = "", content = "";

                        try {
                            name = reader.nextName().matches("name") ? reader.nextString() : "";
                            content = reader.nextName().matches("content") ? reader.nextString() : "";
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }

                        faqs.add(new FaqData(name, content));
                        reader.endObject();
                    }
                    reader.endArray();
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    return e.getMessage();
                }

                return null;
            }

            @Override
            protected void done(@Nullable String result) {
                if (getContext() != null) {
                    if (result != null)
                        Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();

                    progressBar.setVisibility(View.GONE);
                    if (faqs.size() > 0) {
                        adapter = new FaqAdapter(getContext(), faqs);
                        recycler.setAdapter(adapter);
                    } else emptyView.setVisibility(View.VISIBLE);
                }
            }
        }.execute();

        v.findViewById(R.id.community).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(COMMUNITY_URL)));
            }
        });

        return v;
    }

    @Override
    public void filter(@Nullable String filter) {
        if (adapter != null) adapter.filter(filter);
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.tab_help);
    }
}
