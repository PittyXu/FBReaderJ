package org.geometerplus.android.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import org.geometerplus.zlibrary.ui.android.BuildConfig;
import org.json.JSONObject;

import java.util.Set;


public class DefaultPreferences {

  private static final boolean DEBUG = BuildConfig.DEBUG;
  private static final String TAG = "BasePreferences";
  private static final int DEFAULT_CACHE_COUNT = 20;

  private SharedPreferences mPreferences;
  private final LruCache<String, Object> mCached;

  private static class SingletonHolder {

    private static final DefaultPreferences INSTANCE = new DefaultPreferences();
  }

  public static DefaultPreferences getInstance(Context pContext) {
    DefaultPreferences preferences = DefaultPreferences.SingletonHolder.INSTANCE;
    if (preferences.mPreferences == null) {
      preferences.mPreferences = PreferenceManager.getDefaultSharedPreferences(
          pContext.getApplicationContext());
    }
    return preferences;
  }

  private DefaultPreferences() {
    this(DEFAULT_CACHE_COUNT);
  }

  private DefaultPreferences(int cacheCount) {
    this(null, cacheCount);
  }

  private DefaultPreferences(SharedPreferences pPreferences, int cacheCount) {
    mPreferences = pPreferences;
    mCached = new LruCache<>(cacheCount);
  }

  public void setObject(String key, Object obj) {
    String str = JSON.toJSONString(obj);
    if (DEBUG) {
      Log.d(TAG, key + ":" + str);
    }
    mCached.put(key, obj);
    mPreferences.edit().putString(key, str).apply();
  }

  public <T> T getObject(String key, Class<T> cls) {
    T obj = (T) mCached.get(key);
    if (null == obj) {
      String res = mPreferences.getString(key, "");
      obj = JSON.parseObject(res, cls);
      if (null != obj) {
        mCached.put(key, obj);
      }
    }
    return obj;
  }

  //long类型获取
  public void setLong(String key, long value) {
    if (DEBUG) {
      Log.d(TAG, key + ":" + value);
    }
    mCached.put(key, value);
    mPreferences.edit().putLong(key, value).apply();
  }

  public long getLong(String key) {
    Object obj = mCached.get(key);
    if (null == obj) {
      obj = mPreferences.getLong(key, -1L);
      mCached.put(key, obj);
    }
    return (Long) obj;
  }

  public void setString(String key, String value) {
    if (DEBUG) {
      Log.d(TAG, key + ":" + value);
    }
    if (null != value) {
      mCached.put(key, value);
    }
    mPreferences.edit().putString(key, value).apply();
  }

  public String getString(String key) {
    return getString(key, null);
  }

  public String getString(final String key, final String def) {
    Object obj = mCached.get(key);
    if (null == obj) {
      obj = mPreferences.getString(key, null);
      if (null == obj) {
        obj = def;
      }
      if (null != obj) {
        mCached.put(key, obj);
      }
    }
    return (String) obj;
  }

  public void setStringSet(String key, Set<String> value) {
    if (DEBUG) {
      Log.d(TAG, key + ":" + value);
    }
    if (null != value) {
      mCached.put(key, value);
    }
    mPreferences.edit().putStringSet(key, value).apply();
  }

  public Set<String> getStringSet(String key) {
    Object obj = mCached.get(key);
    if (null == obj) {
      obj = mPreferences.getStringSet(key, null);
      if (null != obj) {
        mCached.put(key, obj);
      }
    }
    return (Set<String>) obj;
  }

  public void setInt(String key, int value) {
    if (DEBUG) {
      Log.d(TAG, key + ":" + value);
    }
    mCached.put(key, value);
    mPreferences.edit().putInt(key, value).apply();
  }

  public int getInt(String key) {
    return getInt(key, -1);
  }

  public int getInt(String key, int def) {
    Object obj = mCached.get(key);
    if (null == obj) {
      obj = mPreferences.getInt(key, def);
      mCached.put(key, obj);
    }
    return (Integer) obj;
  }

  public void setBoolean(String key, boolean value) {
    if (DEBUG) {
      Log.d(TAG, key + ":" + value);
    }
    mCached.put(key, value);
    mPreferences.edit().putBoolean(key, value).apply();
  }

  public boolean getBoolean(String key) {
    Object obj = mCached.get(key);
    if (null == obj) {
      obj = mPreferences.getBoolean(key, false);
      mCached.put(key, obj);
    }
    return (boolean) obj;
  }

  public boolean getBoolean(String key, boolean def) {
    Object obj = mCached.get(key);
    if (null == obj) {
      obj = mPreferences.getBoolean(key, def);
      mCached.put(key, obj);
    }
    return (boolean) obj;
  }

  /** 设置带有时间戳的文件字符串 */
  public void setLimitString(String key, String value, long maxTime) {
    setString(key, (System.currentTimeMillis() + maxTime) + "@" + value);
  }

  /** 获得带有时间戳限制的文件字符串 */
  public String getLimitString(String key) {
    String value = getString(key);
    if (TextUtils.isEmpty(value)) {
      return null;
    }
    String[] strings = value.split("@", 2);
    if (strings.length < 2) {
      return null;
    }

    long lastTime = Long.valueOf(strings[0]);
    if (System.currentTimeMillis() < lastTime) {
      return strings[1];
    }
    return null;
  }

  public void clear() {
    mCached.evictAll();
    mPreferences.edit().clear().apply();
  }
}
