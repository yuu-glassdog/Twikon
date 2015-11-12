package com.example.mytwitter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.loopj.android.image.SmartImageView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import twitter4j.EntitySupport;
import twitter4j.ExtendedMediaEntity;
import twitter4j.MediaEntity;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


public class MainActivity extends ListActivity {
	
	private TweetAdapter mAdapter;
	private Twitter mTwitter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// アクセストークンが保存されていなければ(認証済みでなければ) TwitterOAuthActivity を呼び出す
		if (!TwitterUtils.hasAccessToken(this)) {
			Intent intent = new Intent(this, TwitterOAuthActivity.class);
			startActivity(intent);
			finish();	// MainActivityを終了(Backしても大丈夫)
		// 
		} else {
			mAdapter = new TweetAdapter(this);
			setListAdapter(mAdapter);
			
			mTwitter = TwitterUtils.getTwitterInstance(this);
			reloadTimeLine();
		}
	}
	
	// リストからツイートを選択してリツイート
	@Override
	protected void onListItemClick (ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		mTwitter = TwitterUtils.getTwitterInstance(this);
		// 選択したツイートのStatus(情報)を取得
		final twitter4j.Status target = (twitter4j.Status) l.getAdapter().getItem(position);
		// ダイアログ表示
		AlertDialog.Builder dlg;
		dlg = new AlertDialog.Builder(MainActivity.this);
		dlg.setTitle("確認");
		dlg.setMessage("リツイートしますか？");
		// OKの処理
		dlg.setPositiveButton("OK",
			new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>() {
		            @Override
		            protected Boolean doInBackground(String... params) {
		                try {
		                	// ツイートを投稿
		                	mTwitter.retweetStatus(target.getId());
		                    return true;
		                } catch (TwitterException e) {
		                    e.printStackTrace();
		                    return false;
		                }
		            }
		            // 結果の判定と出力
		            @Override
		            protected void onPostExecute(Boolean result) {
		                if (result) {
		                    showToast("リツイートが完了しました！");
		                } else {
		                    showToast("ERROR : リツイートに失敗しました.\n同じツイートを2回以上リツイートすることはできません.");
		                }
		            }
				};
				task.execute();
			}
		});
		// キャンセルの処理
		dlg.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Cancelの処理(do nothing)
				}
			});
		dlg.show();
	}
	
	// ArrayAdapterを継承したクラス
	private class TweetAdapter extends ArrayAdapter<twitter4j.Status> {
		
		private LayoutInflater mInflater;

	    public TweetAdapter(Context context) {
	        super(context, android.R.layout.simple_list_item_1);
	        mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	    }
	    
	    // 各リストのデータを表示
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        if (convertView == null) {
	            convertView = mInflater.inflate(R.layout.list_item_tweet, null);
	        }
	        Status item = getItem(position);
	        // ユーザ名
	        TextView name = (TextView) convertView.findViewById(R.id.name);
	        name.setText(item.getUser().getName());
	        // ユーザID
	        TextView screenName = (TextView) convertView.findViewById(R.id.screen_name);
	        screenName.setText("@" + item.getUser().getScreenName());
	        // 本文
	        TextView text = (TextView) convertView.findViewById(R.id.text);
	        text.setAutoLinkMask(Linkify.WEB_URLS);	// リンク(URL)の有効化
	        text.setText(item.getText());
	        // アイコン画像
	        SmartImageView icon = (SmartImageView)convertView.findViewById(R.id.icon);
	        icon.setImageUrl(item.getUser().getProfileImageURL());
	        // 添付画像
	        SmartImageView image = (SmartImageView)convertView.findViewById(R.id.img);
	        ExtendedMediaEntity entitys[] = item.getExtendedMediaEntities();
	        if ( entitys.length != 0 ) {
	        	for(int i = 0; i < entitys.length; i++) {
	        		MediaEntity entity = entitys[i];
	        		String mediaURL = entity.getMediaURL();
	        		image.setImageUrl(mediaURL);
		        }
	        }
	        return convertView;
	    }
	}
	
	// タイムラインを取得してListViewに表示する
	private void reloadTimeLine() {
	    AsyncTask<Void, Void, List<twitter4j.Status>> task = new AsyncTask<Void, Void, List<twitter4j.Status>>() {
	        @Override
	        protected List<twitter4j.Status> doInBackground(Void... params) {
	            try {
	                return mTwitter.getHomeTimeline();
	            } catch (TwitterException e) {
	                e.printStackTrace();
	            }
	            return null;
	        }

	        @Override
	        protected void onPostExecute(List<twitter4j.Status> result) {
	            if (result != null) {
	                mAdapter.clear();
	                for (twitter4j.Status status : result) {
	                    mAdapter.add(status);
	                }
	                getListView().setSelection(0);
	            } else {
	                showToast("ERROR : タイムラインの取得に失敗しました.\nネットワークの接続状況を確認してください.");
	            }
	        }
	    };
	    task.execute();
	}
	
	// 結果表示
	private void showToast(String text) {
	    Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
	
	// メニュー
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    	// タイムラインを更新
	        case R.id.menu_refresh:
				reloadTimeLine();
	            return true;
	        // ツイートを投稿
	        case R.id.menu_tweet:
	        	Intent intent = new Intent(this, TweetActivity.class);
	        	startActivity(intent);
	        	return true;
	    }
	    return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
