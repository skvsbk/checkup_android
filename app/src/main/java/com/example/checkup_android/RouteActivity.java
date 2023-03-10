package com.example.checkup_android;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class RouteActivity extends AppCompatActivity {
    private static final String PREFS_FILE = "Settings";
    private static final String PREF_URLAPI = "UrlAPI";
    SharedPreferences settings;
    String urlAPIServer;
    SingleVars vars = SingleVars.getInstance();
    AsyncHttpClient httpClient = new AsyncHttpClient();

    ArrayList<Routes> routes = new ArrayList<>();
    ArrayList<Checkups> checkups = new ArrayList<>();
    TextView textView_total, textView_success, textView_canceled;
    Button button_start_check;
    Integer facility_id;

    RecyclerView recyclerView_Routes, recyclerView_Ckeckups;
    RoutesAdapter adapter_routes;
    CheckupsAdapter adapter_checkups;

    JSONArray routeArrayForDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        settings = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
        urlAPIServer = settings.getString(PREF_URLAPI, "");
        facility_id = vars.getIntvars("facility_id");

        textView_total = findViewById(R.id.textView_total);
        textView_success = findViewById(R.id.textView_success);
        textView_canceled = findViewById(R.id.textView_canceled);
        button_start_check = findViewById(R.id.button_start_check);
//        button_start_check.setEnabled(false);
        getTotalChecups();

        // начальная инициализация списка
        fillRoutesListView();
        fillCheckupsListView();
        recyclerView_Routes = findViewById(R.id.list_routes);
        recyclerView_Ckeckups = findViewById(R.id.list_checkups);

        // создаем адаптер
        adapter_routes = new RoutesAdapter(this, routes);
        adapter_checkups = new CheckupsAdapter(this, checkups);
        // устанавливаем для списка адаптер
        recyclerView_Routes.setAdapter(adapter_routes);
        recyclerView_Ckeckups.setAdapter(adapter_checkups);


    }

    private void fillRoutesListView() {
//        http://127.0.0.1:8000/routes/?facility_id=1
        String queryAPIroutes = urlAPIServer + "/routes/?facility_id=" + facility_id;
        httpClient.get(queryAPIroutes, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                try {
                    routeArrayForDB = new JSONArray(result);
                    String route_name = "";
                    for (int i = 0; i < routeArrayForDB.length(); i++){
                        JSONObject o = routeArrayForDB.getJSONObject(i);
                        route_name = o.getString("name");
                        routes.add(new Routes(route_name));
                    }
                    adapter_routes.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), R.string.alert_fail, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillCheckupsListView() {
        //  http://0.0.0.0:8000/checkup_headers/last?user_id=18&limit=10
        String user_id = vars.getIntvars("user_id").toString();
        String limit = "10";
        String queryAPI = urlAPIServer + "/checkup_headers/last?user_id=" + user_id + "&limit=" + limit;
        httpClient.get(queryAPI, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                try {
                    JSONArray responseArray = new JSONArray(result);

                    for (int i = 0; i < responseArray.length(); i++){
                        JSONObject o = responseArray.getJSONObject(i);
                        checkups.add(new Checkups(o.getString("time_start"),
                                o.getString("facility_name"), o.getString("route_name")));
                    }
                    adapter_checkups.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    public void onClickButtonStart(View view){
        // alert message with selected route
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(RouteActivity.this);
        alertBuilder.setTitle(R.string.dialog_title)
                .setMessage(getString(R.string.dialog_route_select) + vars.getStrVars("route_name"))
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String route_id = Utils.getIdFromArray(routeArrayForDB, vars.getStrVars("route_name"));
                        vars.setIntVars("route_id", Integer.valueOf(route_id));
                        Toast.makeText(getApplicationContext(), "Выбран маршрут: " + vars.getStrVars("route_name"), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Выбираем дольше", Toast.LENGTH_LONG).show();
                    }
                });
        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter_routes.notifyDataSetChanged();
    }

    private void getTotalChecups() {
//        http://0.0.0.0:8000/checkup_headers/count/user_id/18
        String user_id = vars.getIntvars("user_id").toString();
        String queryAPI = urlAPIServer + "/checkup_headers/count/user_id/" + user_id;
        httpClient.get(queryAPI, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                try {
                    JSONObject response = new JSONObject(result);
                    textView_total.setText(response.getString("total"));
                    textView_success.setText(response.getString("finished"));
                    textView_canceled.setText(response.getString("not_finished"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }
}

//todo
// Done  1. API на созранение checkup_header (done) и checkup_detail (done).
// Done 2. API на получение route_link с val_params с наименованиями.
// 3. Активити для маршрута: Пройдено/осталось, список точек с параметрами, кнопка Сохранить, кнопка Создать инцидент (опция)
// 4. Создание записи в checkup_headers
// Done 5. API Update  checkup_headers (time_finish, is_completed)
// 6. Сделать последние 10 обходов с фоном по статусу (зеленый/красный). (API done)
// Done 7. Выборка по обходам (всего, законченных, незаконченных). (API done)

