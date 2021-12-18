package com.zzx.phone.utils;

import java.util.HashMap;

import de.greenrobot.event.EventBus;

/**@author Tomy
 * Created by Tomy on 2014/6/20.
 */
public class EventBusUtils {
    private static HashMap<String, EventBus> mBusMap = null;

    private EventBusUtils() {
        if (mBusMap == null) {
            mBusMap = new HashMap<String, EventBus>();
        }
    }

    public static void getInstance() {
        new EventBusUtils();
    }
    /**添加EventBus
     * @param event 该EventBus的事件类型
     * */
    private static void addEventBus(String event) {
        if (mBusMap == null) {
            getInstance();
        }
        if (mBusMap.get(event) == null) {
            mBusMap.put(event, new EventBus());
        }
    }

    /**获取该类型事件的EventBus,若当前没有则即时加入
     * */
    private static EventBus getEventBus(String event) {
        addEventBus(event);
        return mBusMap.get(event);
    }

    /**清除该类型EventBus
     * */
    public static void removeEventBus(String event) {
        if (mBusMap != null)
            mBusMap.remove(event);
    }

    /**清除所有Bus
     * */
    public static void release() {
        if (mBusMap != null) {
            mBusMap.clear();
            mBusMap = null;
        }
    }

    /**注册事件
     * */
    public static void registerEvent(String busEvent, Object event) {
        getEventBus(busEvent).register(event);
    }

    /**注册Sticky事件
     * */
    public static void registerStickyEvent(String busEvent, Object event) {
        getEventBus(busEvent).registerSticky(event);
    }

    /**注销事件
     * */
    public static void removeEvent(String busEvent, Object event) {
        getEventBus(busEvent).unregister(event);
    }

    /**注销Sticky事件
     * */
    public static void removeStickyEvent(String busEvent, Object event) {
        getEventBus(busEvent).removeStickyEvent(event);
    }

    /**发送事件
     * */
    public static void postEvent(String busEvent, Object event) {
        getEventBus(busEvent).post(event);
    }

    public static void postEvent(String busEvent, int event) {
        getEventBus(busEvent).post(new Integer(event));
    }

    public static void postEvent(String busEvent, float event) {
        getEventBus(busEvent).post(new Float(event));
    }

    public static void postEvent(String busEvent, double event) {
        getEventBus(busEvent).post(new Double(event));
    }

    /**发送Sticky事件
     * */
    public static void postStickyEvent(String busEvent, Object event) {
        getEventBus(busEvent).postSticky(event);
    }
    public void onEventBackgroundThread() {

    }

    public void onEvent() {

    }

    public void onEventAsync() {

    }

    public void onEventMainThread() {}
}
