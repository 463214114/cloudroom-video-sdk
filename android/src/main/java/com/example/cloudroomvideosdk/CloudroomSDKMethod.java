package com.example.cloudroomvideosdk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Environment;
import com.cloudroom.cloudroomvideosdk.CRMeetingCallback;
import com.cloudroom.cloudroomvideosdk.CRMgrCallback;
import com.cloudroom.cloudroomvideosdk.CloudroomVideoMeeting;
import com.cloudroom.cloudroomvideosdk.CloudroomVideoMgr;
import com.cloudroom.cloudroomvideosdk.CloudroomVideoSDK;
import com.cloudroom.cloudroomvideosdk.VideoUIView;
import com.cloudroom.cloudroomvideosdk.model.ASTATUS;
import com.cloudroom.cloudroomvideosdk.model.AudioCfg;
import com.cloudroom.cloudroomvideosdk.model.CRVIDEOSDK_ERR_DEF;
import com.cloudroom.cloudroomvideosdk.model.CRVIDEOSDK_MEETING_DROPPED_REASON;
import com.cloudroom.cloudroomvideosdk.model.LoginDat;
import com.cloudroom.cloudroomvideosdk.model.MEDIA_STATE;
import com.cloudroom.cloudroomvideosdk.model.MEDIA_STOP_REASON;
import com.cloudroom.cloudroomvideosdk.model.MIXER_OUTPUT_TYPE;
import com.cloudroom.cloudroomvideosdk.model.MIXER_STATE;
import com.cloudroom.cloudroomvideosdk.model.MIXER_VCONTENT_TYPE;
import com.cloudroom.cloudroomvideosdk.model.MediaInfo;
import com.cloudroom.cloudroomvideosdk.model.MeetInfo;
import com.cloudroom.cloudroomvideosdk.model.MemberInfo;
import com.cloudroom.cloudroomvideosdk.model.MixerCfg;
import com.cloudroom.cloudroomvideosdk.model.MixerCotent;
import com.cloudroom.cloudroomvideosdk.model.MixerOutPutCfg;
import com.cloudroom.cloudroomvideosdk.model.MixerOutputInfo;
import com.cloudroom.cloudroomvideosdk.model.SDK_LOG_LEVEL_DEF;
import com.cloudroom.cloudroomvideosdk.model.ScreenShareCfg;
import com.cloudroom.cloudroomvideosdk.model.SdkInitDat;
import com.cloudroom.cloudroomvideosdk.model.Size;
import com.cloudroom.cloudroomvideosdk.model.UsrVideoId;
import com.cloudroom.cloudroomvideosdk.model.UsrVideoInfo;
import com.cloudroom.cloudroomvideosdk.model.VSTATUS;
import com.cloudroom.cloudroomvideosdk.model.VideoCfg;
import com.example.cloudroomvideosdk.CloudroomMediaView;
import com.example.cloudroomvideosdk.CloudroomPlatformView;
import com.example.cloudroomvideosdk.CloudroomPlatformViewFactory;
import com.example.cloudroomvideosdk.CloudroomScreenShareView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.flutter.Log;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudroomSDKMethod {

  private static String TAG = "FlutterSDK";
  private static EventChannel.EventSink eventSink;
  private static Gson gson = new Gson();
  private static final HashMap<String, Result> resultHashMap = new HashMap<>();

  private static final String External_Storage_Dir = Environment
    .getExternalStorageDirectory()
    .getAbsolutePath();

  public static void getExternalStorageDir(MethodCall call, Result result) {
    result.success(External_Storage_Dir);
  }

  public static void CRLog(MethodCall call, Result result) {
    int level = (int) call.argument("level");
    String log = call.argument("log");
    SDK_LOG_LEVEL_DEF[] loglevels = SDK_LOG_LEVEL_DEF.values();
    SDK_LOG_LEVEL_DEF loglevel = loglevels[level];
    CloudroomVideoSDK.getInstance().writeLog(loglevel, TAG + ":" + log);
    result.success("");
  }

  public static void GetCloudroomVideoSDKVer(MethodCall call, Result result) {
    String sdkVer = CloudroomVideoSDK.getInstance().GetCloudroomVideoSDKVer();
    result.success(sdkVer);
  }

  public static void init(
    MethodCall call,
    Result result,
    Context context,
    EventChannel.EventSink evtSink
  ) {
    eventSink = evtSink;
    String sdkInitDatjson = call.argument("sdkInitDat");
    SdkInitDat initDat = gson.fromJson(sdkInitDatjson, SdkInitDat.class);
    // ?????????SDK
    CRVIDEOSDK_ERR_DEF ret = CloudroomVideoSDK
      .getInstance()
      .init(context, initDat);
    if (ret == CRVIDEOSDK_ERR_DEF.CRVIDEOSDK_NOERR) {
      CloudroomVideoMgr.getInstance().registerCallback(mMgrCallback);
      CloudroomVideoMeeting.getInstance().registerCallback(mMeetingCallback);
    }
    result.success(ret.value());
  }

  public static void uninit(MethodCall call, Result result) {
    CloudroomVideoMgr.getInstance().unregisterCallback(mMgrCallback);
    CloudroomVideoMeeting.getInstance().unregisterCallback(mMeetingCallback);
    CloudroomVideoSDK.getInstance().uninit();
    result.success("");
  }

  public static void getServerAddr(MethodCall call, Result result) {
    String serverAddr = CloudroomVideoSDK.getInstance().serverAddr();
    result.success(serverAddr);
  }

  public static void setServerAddr(MethodCall call, Result result) {
    String serverAddr = call.argument("serverAddr");
    CloudroomVideoSDK.getInstance().setServerAddr(serverAddr);
    result.success("");
  }

  public static void login(MethodCall call, Result result) {
    String loginDatjson = call.argument("loginDat");
    String cookie = (String) call.argument("cookie");
    LoginDat dat = gson.fromJson(loginDatjson, LoginDat.class);
    CloudroomVideoMgr.getInstance().login(dat, cookie);
    resultHashMap.put(cookie, result);
  }

  public static void logout(MethodCall call, Result result) {
    CloudroomVideoMgr.getInstance().logout();
    result.success("");
  }

  public static void getMyUserID(MethodCall call, Result result) {
    String userID = CloudroomVideoMeeting.getInstance().getMyUserID();
    result.success(userID);
  }

  // ?????????????????????????????????
  public static void getAllMembers(MethodCall call, Result result) {
    ArrayList<MemberInfo> memberInfos = CloudroomVideoMeeting
      .getInstance()
      .getAllMembers();
    String memberInfosJson = gson.toJson(memberInfos);
    result.success(memberInfosJson);
  }

  // ???????????????????????????
  public static void getMemberInfo(MethodCall call, Result result) {
    String userID = call.argument("userID");
    MemberInfo memberInfo = CloudroomVideoMeeting
      .getInstance()
      .getMemberInfo(userID);
    result.success(gson.toJson(memberInfo));
  }

  // ???????????????????????????
  public static void getNickName(MethodCall call, Result result) {
    String userID = call.argument("userID");
    String nickname = CloudroomVideoMeeting.getInstance().getNickName(userID);
    result.success(nickname);
  }

  // ???????????????????????????
  public static void setNickName(MethodCall call, Result result) {
    String userID = call.argument("userID");
    String nickname = call.argument("nickName");
    CloudroomVideoMeeting.getInstance().setNickName(userID, nickname);
    resultHashMap.put("setNickName", result);
  }

  //????????????????????????????????????
  public static void isUserInMeeting(MethodCall call, Result result) {
    String userID = call.argument("userID");
    boolean isUserInMeeting = CloudroomVideoMeeting
      .getInstance()
      .isUserInMeeting(userID);
    result.success(isUserInMeeting);
  }

  // ????????????
  public static void createMeeting(MethodCall call, Result result) {
    String cookie = (String) call.argument("cookie");
    CloudroomVideoMgr.getInstance().createMeeting(cookie);
    resultHashMap.put(cookie, result);
  }

  // ????????????
  public static void enterMeeting(MethodCall call, Result result) {
    int meetID = (int) call.argument("meetID");
    // String pswd = call.argument("pswd");
    // String userID = call.argument("userID");
    // String nickName = call.argument("nickName");
    // String cookie = call.argument("cookie");
    CloudroomVideoMeeting.getInstance().enterMeeting(meetID);
    resultHashMap.put("enterMeeting", result);
  }

  // ????????????
  public static void destroyMeeting(MethodCall call, Result result) {
    int meetID = (int) call.argument("meetID");
    String cookie = call.argument("cookie");
    CloudroomVideoMgr.getInstance().destroyMeeting(meetID, cookie);
    resultHashMap.put(cookie, result);
  }

  // ????????????
  public static void exitMeeting(MethodCall call, Result result) {
    CloudroomVideoMeeting.getInstance().exitMeeting();
    result.success("");
  }

  /**
   * ?????????
   */

  // ???????????????
  public static void openMic(MethodCall call, Result result) {
    String userID = call.argument("userID");
    CloudroomVideoMeeting.getInstance().openMic(userID);
    result.success("");
  }

  // ???????????????
  public static void closeMic(MethodCall call, Result result) {
    String userID = call.argument("userID");
    CloudroomVideoMeeting.getInstance().closeMic(userID);
    result.success("");
  }

  // ???????????????????????????
  public static void getAudioCfg(MethodCall call, Result result) {
    String userID = call.argument("userID");
    AudioCfg audioCfg = CloudroomVideoMeeting.getInstance().getAudioCfg();
    result.success(gson.toJson(audioCfg));
  }

  // ???????????????????????????
  public static void setAudioCfg(MethodCall call, Result result) {
    String audioCfgJson = call.argument("audioCfg");
    AudioCfg acfg = gson.fromJson(audioCfgJson, AudioCfg.class);
    CloudroomVideoMeeting.getInstance().setAudioCfg(acfg);
    result.success("");
  }

  // ????????????????????????
  public static void getAudioStatus(MethodCall call, Result result) {
    String userID = call.argument("userID");
    ASTATUS AStatus = CloudroomVideoMeeting
      .getInstance()
      .getAudioStatus(userID);
    result.success(AStatus.value());
  }

  // ???????????????????????????
  public static void getMicVolume(MethodCall call, Result result) {
    int volume = CloudroomVideoMeeting.getInstance().getMicVolume();
    result.success(volume);
  }

  // ??????????????????
  public static void getSpeakerOut(MethodCall call, Result result) {
    boolean isSpeakerOut = CloudroomVideoMeeting.getInstance().getSpeakerOut();
    result.success(isSpeakerOut);
  }

  // ??????????????????
  public static void setSpeakerOut(MethodCall call, Result result) {
    boolean speakerOut = (boolean) call.argument("speakerOut");
    boolean isSpeakerOut = CloudroomVideoMeeting
      .getInstance()
      .setSpeakerOut(speakerOut);
    result.success(isSpeakerOut);
  }

  // ???????????????????????????
  public static void getSpeakerVolume(MethodCall call, Result result) {
    int speakerVolume = CloudroomVideoMeeting.getInstance().getSpeakerVolume();
    result.success(speakerVolume);
  }

  // ???????????????????????????
  public static void setSpeakerVolume(MethodCall call, Result result) {
    int speakerVolume = (int) call.argument("speakerVolume");
    boolean isSetSpeakerVolumeSuccess = CloudroomVideoMeeting
      .getInstance()
      .setSpeakerVolume(speakerVolume);
    result.success(isSetSpeakerVolumeSuccess);
  }

  // ????????????????????????
  public static void getSpeakerMute(MethodCall call, Result result) {
    boolean isSpeakerMute = CloudroomVideoMeeting
      .getInstance()
      .getSpeakerMute();
    result.success(isSpeakerMute);
  }

  // ????????????????????????
  public static void setSpeakerMute(MethodCall call, Result result) {
    boolean mute = (boolean) call.argument("mute");
    CloudroomVideoMeeting.getInstance().setSpeakerMute(mute);
    result.success(mute);
  }

  /**
   * ??????
   */
  // ?????????????????????????????????
  public static void setUsrVideoId(MethodCall call, Result result) {
    int viewID = (int) call.argument("viewID");
    String usrVideoIdStr = call.argument("usrVideoId");
    UsrVideoId usrVideoId = gson.fromJson(usrVideoIdStr, UsrVideoId.class);
    CloudroomPlatformView platformView = CloudroomPlatformViewFactory
      .getInstance()
      .getPlatformView(viewID);
    if (platformView != null) {
      VideoUIView view = platformView.getVideoUIView();
      view.setUsrVideoId(usrVideoId);
    }
    result.success("");
  }

  // ??????????????????
  public static void destroyPlatformView(MethodCall call, Result result) {
    int viewID = (int) call.argument("viewID");
    Boolean isDestroy = CloudroomPlatformViewFactory
      .getInstance()
      .destroyPlatformView(viewID);
    result.success(isDestroy);
  }

  // ??????????????????
  public static void getScaleType(MethodCall call, Result result) {
    int viewID = (int) call.argument("viewID");
    CloudroomPlatformView platformView = CloudroomPlatformViewFactory
      .getInstance()
      .getPlatformView(viewID);
    if (platformView != null) {
      VideoUIView view = platformView.getVideoUIView();
      int scaleType = view.getScaleType();
      result.success(scaleType);
    } else {
      result.success(-1);
    }
  }

  // ??????????????????
  public static void setScaleType(MethodCall call, Result result) {
    int viewID = (int) call.argument("viewID");
    int scaleType = (int) call.argument("scaleType");
    CloudroomPlatformView platformView = CloudroomPlatformViewFactory
      .getInstance()
      .getPlatformView(viewID);
    if (platformView != null) {
      VideoUIView view = platformView.getVideoUIView();
      view.setScaleType(scaleType);
    }
    result.success("");
  }

  // ???????????????
  public static void openVideo(MethodCall call, Result result) {
    String userID = call.argument("userID");
    CloudroomVideoMeeting.getInstance().openVideo(userID);
    resultHashMap.put("openVideo", result);
  }

  // ???????????????
  public static void closeVideo(MethodCall call, Result result) {
    String userID = call.argument("userID");
    CloudroomVideoMeeting.getInstance().closeVideo(userID);
    result.success("");
  }

  // ??????????????????????????????
  public static void getVideoCfg(MethodCall call, Result result) {
    VideoCfg videoCfg = CloudroomVideoMeeting.getInstance().getVideoCfg();
    result.success(gson.toJson(videoCfg));
  }

  // ?????????????????????
  public static void setVideoCfg(MethodCall call, Result result) {
    String videoCfgJson = call.argument("videoCfg");
    VideoCfg vcfg = gson.fromJson(videoCfgJson, VideoCfg.class);
    CloudroomVideoMeeting.getInstance().setVideoCfg(vcfg);
    result.success("");
  }

  // ??????????????????????????????
  public static void getWatchableVideos(MethodCall call, Result result) {
    ArrayList<UsrVideoId> watchableVideos = CloudroomVideoMeeting
      .getInstance()
      .getWatchableVideos();
    result.success(gson.toJson(watchableVideos));
  }

  // ????????????????????????????????????
  public static void getAllVideoInfo(MethodCall call, Result result) {
    String userID = call.argument("userID");
    ArrayList<UsrVideoInfo> videosInfo = CloudroomVideoMeeting
      .getInstance()
      .getAllVideoInfo(userID);
    result.success(gson.toJson(videosInfo));
  }

  // ???????????????????????????????????? ???????????????????????????????????????0
  public static void getDefaultVideo(MethodCall call, Result result) {
    String userID = call.argument("userID");
    short defaultVideo = CloudroomVideoMeeting
      .getInstance()
      .getDefaultVideo(userID);
    result.success(defaultVideo);
  }

  // ????????????????????????
  public static void setDefaultVideo(MethodCall call, Result result) {
    String userID = call.argument("userID");
    Integer videoID = call.argument("videoID");
    short vid = videoID.shortValue();
    CloudroomVideoMeeting.getInstance().setDefaultVideo(userID, vid);
    result.success("");
  }

  /**
   * ????????????
   */
  // ?????????????????????????????????
  public static void isScreenShareStarted(MethodCall call, Result result) {
    Boolean isScreenShareStarted = CloudroomVideoMeeting
      .getInstance()
      .isScreenShareStarted();
    result.success(isScreenShareStarted);
  }

  // ????????????????????????
  public static void getScreenShareCfg(MethodCall call, Result result) {
    ScreenShareCfg screenShareCfg = CloudroomVideoMeeting
      .getInstance()
      .getScreenShareCfg();
    String screenShareCfgJson = gson.toJson(screenShareCfg);
    result.success(screenShareCfgJson);
  }

  // ????????????????????????
  public static void setScreenShareCfg(MethodCall call, Result result) {
    String ssCfgStr = call.argument("screenShareCfg");
    ScreenShareCfg ssCfg = gson.fromJson(ssCfgStr, ScreenShareCfg.class);
    CloudroomVideoMeeting.getInstance().setScreenShareCfg(ssCfg);
    result.success("");
  }

  // ??????????????????
  public static void startScreenShare(MethodCall call, Result result) {
    CloudroomVideoMeeting.getInstance().startScreenShare();
    resultHashMap.put("startScreenShare", result);
  }

  // ??????????????????
  public static void stopScreenShare(MethodCall call, Result result) {
    CloudroomVideoMeeting.getInstance().stopScreenShare();
    resultHashMap.put("stopScreenShare", result);
  }

  // ????????????
  public static void startScreenMark(MethodCall call, Result result) {
    CloudroomVideoMeeting.getInstance().startScreenMark();
    result.success("");
  }

  // ????????????
  public static void stopScreenMark(MethodCall call, Result result) {
    CloudroomVideoMeeting.getInstance().stopScreenMark();
    result.success("");
  }

  // ????????????????????????
  public static void destroyScreenShareView(MethodCall call, Result result) {
    int viewID = (int) call.argument("viewID");
    Boolean isDestroy = CloudroomPlatformViewFactory
      .getInstance()
      .destroyScreenShareView(viewID);
    result.success(isDestroy);
  }

  /**
   * ??????
   */
  private static ArrayList<MixerCotent> addContents(
    ArrayList<Map> mixerCotentRects
  ) {
    // ??????????????????
    ArrayList<MixerCotent> contents = new ArrayList<MixerCotent>();
    for (Map mcr : mixerCotentRects) {
      String mUserId = (String) mcr.get("userId");
      int mtype = ((Double) mcr.get("type")).intValue();
      int mwidth = ((Double) mcr.get("width")).intValue();
      int mheight = ((Double) mcr.get("height")).intValue();
      int mleft = ((Double) mcr.get("left")).intValue();
      int mtop = ((Double) mcr.get("top")).intValue();
      int mright = mleft + mwidth;
      int mbottom = mtop + mheight;
      Rect rect = new Rect(mleft, mtop, mright, mbottom);
      if (mtype == 0) {
        short mcamId = ((Double) mcr.get("camId")).shortValue();
        MixerCotent mCotent = MixerCotent.createVideoContent(
          mUserId,
          mcamId,
          rect
        );
        contents.add(mCotent);
      } else if (mtype == 1) {
        String mresId = (String) mcr.get("resId");
        MixerCotent mCotent = MixerCotent.createPicContent(mresId, rect);
        contents.add(mCotent); // ?????????????????????
      } else if (mtype == 2) {
        MixerCotent mCotent = MixerCotent.createScreenContent(rect);
        contents.add(mCotent);
      } else if (mtype == 3) {
        MixerCotent mCotent = MixerCotent.createMediaContent(rect);
        contents.add(mCotent);
      } else if (mtype == 4) {
        MixerCotent mCotent = new MixerCotent(
          MIXER_VCONTENT_TYPE.MIXVTP_TIMESTAMP,
          rect
        );
        contents.add(mCotent);
      } else if (mtype == 5) {
        MixerCotent mCotent = MixerCotent.createRemoteScreenContent(rect);
        contents.add(mCotent);
      } else if (mtype == 7) {
        String mtext = (String) mcr.get("text");
        MixerCotent mCotent = MixerCotent.createTextContent(mtext, rect);
        contents.add(mCotent);
      }
    }

    return contents;
  }

  // ???????????????
  public static void createLocMixer(MethodCall call, Result result) {
    String mixerCfgJson = call.argument("mixerCfg");
    String mixerCotentRectJson = call.argument("mixerCotentRects");
    String mixerID = call.argument("mixerID");

    // ?????????????????????
    MixerCfg mixerCfg = gson.fromJson(mixerCfgJson, MixerCfg.class);
    // ?????????????????????
    ArrayList<Map> mixerCotentRects = gson.fromJson(
      mixerCotentRectJson,
      new TypeToken<ArrayList<Map>>() {}.getType()
    );
    // ??????????????????
    ArrayList<MixerCotent> contents = addContents(mixerCotentRects);
    // ???????????????, ?????????????????????
    CRVIDEOSDK_ERR_DEF errCode = CloudroomVideoMeeting
      .getInstance()
      .createLocMixer(mixerID, mixerCfg, contents);
    result.success(errCode.value());
  }

  // ???????????????????????????
  public static void addLocMixerOutput(MethodCall call, Result result) {
    String mixerID = call.argument("mixerID");
    String mixerOutPutCfgsJson = call.argument("mixerOutPutCfgs");
    // ???????????????????????????
    ArrayList<MixerOutPutCfg> cfgs = gson.fromJson(
      mixerOutPutCfgsJson,
      new TypeToken<ArrayList<MixerOutPutCfg>>() {}.getType()
    );
    CRVIDEOSDK_ERR_DEF errCode = CloudroomVideoMeeting
      .getInstance()
      .addLocMixerOutput(mixerID, cfgs);
    result.success(errCode.value());
  }

  // ????????????????????????????????? , ??????????????????????????????????????????????????????????????????????????????????????????
  public static void rmLocMixerOutput(MethodCall call, Result result) {
    String mixerID = call.argument("mixerID");
    String nameOrUrlsJson = call.argument("nameOrUrls");
    ArrayList<String> nameOrUrls = gson.fromJson(
      nameOrUrlsJson,
      new TypeToken<ArrayList<String>>() {}.getType()
    );
    CloudroomVideoMeeting.getInstance().rmLocMixerOutput(mixerID, nameOrUrls);
    result.success("");
  }

  // ???????????????
  public static void updateLocMixerContent(MethodCall call, Result result) {
    String mixerCotentRectJson = call.argument("mixerCotentRects");
    String mixerID = call.argument("mixerID");

    // ?????????????????????
    ArrayList<Map> mixerCotentRects = gson.fromJson(
      mixerCotentRectJson,
      new TypeToken<ArrayList<Map>>() {}.getType()
    );
    // ??????????????????
    ArrayList<MixerCotent> contents = addContents(mixerCotentRects);
    // ??????????????????
    CRVIDEOSDK_ERR_DEF errCode = CloudroomVideoMeeting
      .getInstance()
      .updateLocMixerContent(mixerID, contents);
    result.success(errCode.value());
  }

  // ?????????????????? ??????????????????
  public static void destroyLocMixer(MethodCall call, Result result) {
    String mixerID = call.argument("mixerID");
    CloudroomVideoMeeting.getInstance().destroyLocMixer(mixerID);
    result.success("");
  }

  // ???????????????????????????
  public static void getLocMixerState(MethodCall call, Result result) {
    String mixerID = call.argument("mixerID");
    MIXER_STATE mixerState = CloudroomVideoMeeting
      .getInstance()
      .getLocMixerState(mixerID);
    result.success(mixerState.value());
  }

  // ???????????????????????????????????????
  public static void getSvrMixerState(MethodCall call, Result result) {
    MIXER_STATE mixerState = CloudroomVideoMeeting
      .getInstance()
      .getSvrMixerState();
    result.success(mixerState.value());
  }

  // ??????????????????
  public static void startSvrMixer(MethodCall call, Result result) {
    // ?????????????????????
    String mutiMixerCfg = call.argument("mutiMixerCfg");
    // ???????????????
    String mutiMixerContents = call.argument("mutiMixerContents");
    // ?????????????????????
    String mutiMixerOutput = call.argument("mutiMixerOutput");
    //??????????????????
    CRVIDEOSDK_ERR_DEF errCode = CloudroomVideoMeeting
      .getInstance()
      .startSvrMixer(mutiMixerCfg, mutiMixerContents, mutiMixerOutput);
    result.success(errCode.value());
  }

  // ???????????????????????????????????????
  public static void updateSvrMixerContent(MethodCall call, Result result) {
    // ???????????????
    String mutiMixerContents = call.argument("mutiMixerContents"); //??????????????????
    CRVIDEOSDK_ERR_DEF svruErrCode = CloudroomVideoMeeting
      .getInstance()
      .updateSvrMixerContent(mutiMixerContents);
    result.success(svruErrCode.value());
  }

  // ?????????????????????????????????
  public static void stopSvrMixer(MethodCall call, Result result) {
    CloudroomVideoMeeting.getInstance().stopSvrMixer();
    result.success("");
  }

  /**
   * ????????????
   */
  // ????????????????????????
  public static void destroyMediaView(MethodCall call, Result result) {
    int viewID = (int) call.argument("viewID");
    Boolean isDestroyMediaView = CloudroomPlatformViewFactory
      .getInstance()
      .destroyMediaView(viewID);
    result.success(isDestroyMediaView);
  }

  // ??????????????????????????????
  public static void getMediaCfg(MethodCall call, Result result) {
    VideoCfg cfg = CloudroomVideoMeeting.getInstance().getMediaCfg();
    result.success(gson.toJson(cfg));
  }

  // ????????????????????????????????????????????????
  public static void setMediaCfg(MethodCall call, Result result) {
    String mediaCfgJson = call.argument("mediaCfg");
    VideoCfg cfg = gson.fromJson(mediaCfgJson, VideoCfg.class);
    CloudroomVideoMeeting.getInstance().setMediaCfg(cfg);
    result.success("");
  }

  // ???????????????????????????
  public static void getMediaInfo(MethodCall call, Result result) {
    MediaInfo mediaInfo = CloudroomVideoMeeting.getInstance().getMediaInfo();
    Map data = new HashMap();
    data.put("userID", mediaInfo.userID);
    data.put("mediaName", mediaInfo.mediaName);
    data.put("state", mediaInfo.state.value());
    String mediaInfoJson = gson.toJson(data);
    result.success(mediaInfoJson);
  }

  // ??????????????????????????? ???????????????0-255???
  public static void getMediaVolume(MethodCall call, Result result) {
    String userID = call.argument("userID");
    int volume = CloudroomVideoMeeting.getInstance().getMediaVolume();
    result.success(volume);
  }

  // ??????????????????????????? ???????????????0-255???
  public static void setMediaVolume(MethodCall call, Result result) {
    int volume = (int) call.argument("volume");
    CloudroomVideoMeeting.getInstance().setMediaVolume(volume);
    result.success(volume);
  }

  // ??????????????????????????????????????????????????????bLocPlay???true
  public static void startPlayMedia(MethodCall call, Result result) {
    String videoSrc = call.argument("videoSrc");
    boolean bLocPlay = (boolean) call.argument("bLocPlay");
    CloudroomVideoMeeting.getInstance().startPlayMedia(videoSrc, bLocPlay);
    result.success("");
  }

  //  ?????????????????????
  public static void pausePlayMedia(MethodCall call, Result result) {
    boolean pause = (boolean) call.argument("pause");
    CloudroomVideoMeeting.getInstance().pausePlayMedia(pause);
    result.success(pause);
  }

  //  ??????????????????
  public static void stopPlayMedia(MethodCall call, Result result) {
    CloudroomVideoMeeting.getInstance().stopPlayMedia();
    result.success("");
  }

  // ??????????????????
  public static void setMediaPlayPos(MethodCall call, Result result) {
    int pos = (int) call.argument("pos");
    CloudroomVideoMeeting.getInstance().setMediaPlayPos(pos);
    result.success(pos);
  }

  /**
   * ??????
   */
  // ??????????????????
  public static void sendMeetingCustomMsg(MethodCall call, Result result) {
    String text = call.argument("text");
    String cookie = call.argument("cookie");
    // ??????????????????
    CloudroomVideoMeeting.getInstance().sendMeetingCustomMsg(text, cookie);
    resultHashMap.put(cookie, result);
  }

  /**
   * ??????
   */

  private static void notification(String method, Map map) {
    if (eventSink != null) {
      map.put("method", method);
      eventSink.success(map);
    } else {
      Log.e(TAG, "sink == null");
    }
  }

  private static void notification(String method) {
    if (eventSink != null) {
      Map map = new HashMap();
      map.put("method", method);
      eventSink.success(map);
    } else {
      Log.e(TAG, "sink == null");
    }
  }

  private static void callBackResult(String key, Map data) {
    Result result = resultHashMap.get(key);
    if (result != null) {
      result.success(data);
      resultHashMap.remove(key);
    }
  }

  private static CRMgrCallback mMgrCallback = new CRMgrCallback() {
    // ????????????
    @Override
    public void loginSuccess(String userID, String cookie) {
      Map data = new HashMap();
      data.put("userID", userID);
      data.put("cookie", cookie);
      callBackResult(cookie, data);
    }

    // ????????????
    @Override
    public void loginFail(CRVIDEOSDK_ERR_DEF sdkErr, String cookie) {
      Map data = new HashMap();
      data.put("sdkErr", sdkErr.value());
      data.put("cookie", cookie);
      callBackResult(cookie, data);
    }

    // ??????????????????
    @Override
    public void lineOff(CRVIDEOSDK_ERR_DEF sdkErr) {
      Map result = new HashMap();
      result.put("sdkErr", sdkErr.value());
      notification("lineOff", result);
    }

    // ??????????????????
    @Override
    public void createMeetingSuccess(MeetInfo meetInfo, String cookie) {
      Map map = new HashMap();
      map.put("id", meetInfo.ID);
      map.put("cookie", cookie);
      callBackResult(cookie, map);
    }

    // ??????????????????
    @Override
    public void createMeetingFail(CRVIDEOSDK_ERR_DEF sdkErr, String cookie) {
      Map map = new HashMap();
      map.put("sdkErr", sdkErr.value());
      map.put("cookie", cookie);
      callBackResult(cookie, map);
    }

    // ????????????
    @Override
    public void destroyMeetingRslt(CRVIDEOSDK_ERR_DEF sdkErr, String cookie) {
      Map map = new HashMap();
      map.put("sdkErr", sdkErr.value());
      map.put("cookie", cookie);
      callBackResult(cookie, map);
    }
  };

  private static CRMeetingCallback mMeetingCallback = new CRMeetingCallback() {
    /**
     * ??????????????????
     * @param code
     */
    @Override
    public void enterMeetingRslt(CRVIDEOSDK_ERR_DEF sdkErr) {
      String key = "enterMeeting";
      Result result = resultHashMap.get(key);
      if (result != null) {
        result.success(sdkErr.value());
        resultHashMap.remove(key);
      }
    }

    // ????????????????????????
    @Override
    public void userEnterMeeting(String userID) {
      Map map = new HashMap();
      map.put("userID", userID);
      notification("userEnterMeeting", map);
    }

    // ????????????????????????
    @Override
    public void userLeftMeeting(String userID) {
      Map map = new HashMap();
      map.put("userID", userID);
      notification("userLeftMeeting", map);
    }

    // ?????????????????????????????????
    @Override
    public void setNickNameRsp(
      CRVIDEOSDK_ERR_DEF sdkErr,
      String userID,
      String newName
    ) {
      Map map = new HashMap();
      map.put("userID", userID);
      map.put("newName", newName);
      map.put("sdkErr", sdkErr.value());
      callBackResult("setNickName", map);
    }

    // ????????????????????????(????????????????????????????????????????????????)
    @Override
    public void notifyNickNameChanged(
      String userID,
      String oldName,
      String newName
    ) {
      Map map = new HashMap();
      map.put("userID", userID);
      map.put("oldName", oldName);
      map.put("newName", newName);
      notification("notifyNickNameChanged", map);
    }

    // ???????????????????????????
    @Override
    public void meetingDropped(CRVIDEOSDK_MEETING_DROPPED_REASON reason) {
      Map map = new HashMap();
      map.put("reason", reason.value());
      notification("meetingDropped", map);
    }

    // // ??????????????????
    // @Override
    // public void meetingStoped() {
    //   Map map = new HashMap();
    //   notification("meetingStoped", map);
    // }

    // ??????????????????
    @Override
    public void netStateChanged(int level) {
      Map map = new HashMap();
      map.put("level", level);
      notification("netStateChanged", map);
    }

    @Override
    public void openVideoRslt(String devID, boolean success) {
      // TODO Auto-generated method stub
      Map map = new HashMap();
      map.put("deviceID", devID);
      map.put("success", success);
      callBackResult("openVideo", map);
    }

    // ???????????????????????????
    @Override
    public void videoStatusChanged(
      String userID,
      VSTATUS oldStatus,
      VSTATUS newStatus
    ) {
      Map map = new HashMap();
      map.put("userID", userID);
      map.put("oldStatus", oldStatus.value());
      map.put("newStatus", newStatus.value());
      notification("videoStatusChanged", map);
    }

    @Override
    public void videoDevChanged(String userID) {
      // TODO Auto-generated method stub
      Map map = new HashMap();
      map.put("userID", userID);
      notification("videoDevChanged", map);
    }

    // ????????????????????????
    @Override
    public void audioStatusChanged(
      String userID,
      ASTATUS oldStatus,
      ASTATUS newStatus
    ) {
      Map map = new HashMap();
      map.put("userID", userID);
      map.put("oldStatus", oldStatus.value());
      map.put("newStatus", newStatus.value());
      notification("audioStatusChanged", map);
    }

    @Override
    public void audioDevChanged() {
      notification("audioDevChanged");
    }

    // ???????????????????????????????????????
    @Override
    public void micEnergyUpdate(String userID, int oldLevel, int newLevel) {
      Map map = new HashMap();
      map.put("userID", userID);
      map.put("oldLevel", oldLevel);
      map.put("newLevel", newLevel);
      notification("micEnergyUpdate", map);
    }

    // ??????????????????
    @Override
    public void startScreenShareRslt(CRVIDEOSDK_ERR_DEF sdkErr) {
      String key = "startScreenShare";
      Result result = resultHashMap.get(key);
      if (result != null) {
        result.success(sdkErr.value());
        resultHashMap.remove(key);
      }
    }

    // ??????????????????
    @Override
    public void stopScreenShareRslt(CRVIDEOSDK_ERR_DEF sdkErr) {
      String key = "stopScreenShare";
      Result result = resultHashMap.get(key);
      if (result != null) {
        result.success(sdkErr.value());
        resultHashMap.remove(key);
      }
    }

    // ????????????????????????
    @Override
    public void notifyScreenShareStarted() {
      notification("notifyScreenShareStarted");
    }

    // ????????????????????????
    @Override
    public void notifyScreenShareStopped() {
      notification("notifyScreenShareStopped");
    }

    // ????????????????????????
    @Override
    public void startScreenMarkRslt(CRVIDEOSDK_ERR_DEF sdkErr) {
      Map map = new HashMap();
      map.put("sdkErr", sdkErr.value());
      notification("startScreenMarkRslt", map);
    }

    // ????????????????????????
    @Override
    public void stopScreenMarkRslt(CRVIDEOSDK_ERR_DEF sdkErr) {
      Map map = new HashMap();
      map.put("sdkErr", sdkErr.value());
      notification("stopScreenMarkRslt", map);
    }

    // ??????????????????????????????
    @Override
    public void notifyScreenMarkStarted() {
      notification("notifyScreenMarkStarted");
    }

    // ??????????????????????????????
    @Override
    public void notifyScreenMarkStopped() {
      notification("notifyScreenMarkStopped");
    }

    /**
     * ??????
     */
    // ?????????????????????????????????????????????
    @Override
    public void locMixerOutputInfo(
      String mixerID,
      String nameOrUrl,
      MixerOutputInfo outputInfo
    ) {
      Map map = new HashMap();
      map.put("mixerID", mixerID);
      map.put("nameOrUrl", nameOrUrl);
      Map data = new HashMap();
      data.put("duration", outputInfo.duration);
      data.put("errCode", outputInfo.errCode.value());
      data.put("state", outputInfo.state.value());
      data.put("fileSize", outputInfo.fileSize);
      String outputInfoJson = gson.toJson(data);
      map.put("outputInfo", outputInfoJson);
      notification("locMixerOutputInfo", map);
    }

    // ?????????????????????????????????
    public void locMixerStateChanged(String mixerID, MIXER_STATE state) {
      Map map = new HashMap();
      map.put("mixerID", mixerID);
      map.put("state", state.value());
      notification("locMixerStateChanged", map);
    }

    // ?????????????????????????????????????????????
    @Override
    public void svrMixerStateChanged(
      String operatorID,
      MIXER_STATE state,
      CRVIDEOSDK_ERR_DEF sdkErr
    ) {
      Map map = new HashMap();
      map.put("operatorID", operatorID);
      map.put("state", state.value());
      map.put("sdkErr", sdkErr.value());
      notification("svrMixerStateChanged", map);
    }

    // ?????????????????????????????????????????????
    @Override
    public void svrMixerCfgChanged() {
      notification("svrMixerCfgChanged");
    }

    // ???????????????????????????????????????????????????
    @Override
    public void svrMixerOutputInfo(MixerOutputInfo outputInfo) {
      Map map = new HashMap();
      map.put("outputInfo", gson.toJson(outputInfo));
      notification("svrMixerOutputInfo", map);
    }

    /**
     * ??????
     */
    // ????????????????????????
    @Override
    public void notifyMediaOpened(int totalTime, Size picSZ) {
      Map map = new HashMap();
      map.put("totalTime", totalTime);
      map.put("width", picSZ.width);
      map.put("height", picSZ.height);
      notification("notifyMediaOpened", map);
    }

    // ????????????????????????
    @Override
    public void notifyMediaStart(String userid) {
      Map map = new HashMap();
      map.put("userID", userid);
      notification("notifyMediaStart", map);
    }

    // ????????????????????????
    @Override
    public void notifyMediaPause(String userid, boolean pause) {
      Map map = new HashMap();
      map.put("userID", userid);
      map.put("pause", pause);
      notification("notifyMediaPause", map);
    }

    // ????????????????????????
    @Override
    public void notifyMediaStop(String userid, MEDIA_STOP_REASON reason) {
      Map map = new HashMap();
      map.put("userID", userid);
      map.put("reason", reason.value());
      notification("notifyMediaStop", map);
    }

    //????????????
    @Override
    public void sendMeetingCustomMsgRslt(
      CRVIDEOSDK_ERR_DEF sdkErr,
      String cookie
    ) {
      // TODO Auto-generated method stub
      Map map = new HashMap();
      map.put("sdkErr", sdkErr.value());
      map.put("cookie", cookie);
      callBackResult(cookie, map);
    }

    // ??????????????????
    @Override
    public void notifyMeetingCustomMsg(String fromUserID, String text) {
      // ??????????????????
      Map map = new HashMap();
      map.put("fromUserID", fromUserID);
      map.put("text", text);
      notification("notifyMeetingCustomMsg", map);
    }
  };
}
