package com.chedao.gaode;

import com.chedao.model.*;
import com.chedao.util.EndpointConstants;
import com.chedao.util.EndpointUtils;
import com.chedao.util.OKHttpUtil;
import com.chedao.util.ObjectMapperUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RegeoClient {

  private static final Logger logger = LoggerFactory.getLogger(RegeoClient.class);

  // 高德应用API调用
  private static String geocodeUrl = EndpointUtils.getEndpointProperties().getProperty(EndpointConstants.GEOCODEURL);

  private static OKHttpUtil oKHttpUtil = null;

  private static RegeoClient instance = null;

  private RegeoClient() {}

  public static synchronized RegeoClient getInstance() {
    if (instance == null) {
      instance = new RegeoClient();
    }
    return instance;
  }

  public List<DatahubDeviceData> getRegeoFromResponse(List<DeviceDataPush1007> deviceDataPushListNew, RegeoRequest regeoRequest) {
    DatahubDeviceData datahubDeviceData = new DatahubDeviceData();
    List<DatahubDeviceData> datahubDeviceDataList = new ArrayList<>();
    RegeoResponse regeoResponse = getRegeoResponse(regeoRequest);

    List<Regeocodes> regeoResponseList = regeoResponse.getRegeocodes();

    DeviceDataPush1007 deviceDataPush1007;
    for (int i = 0; i < deviceDataPushListNew.size(); i++) {
      deviceDataPush1007 = deviceDataPushListNew.get(i);
      if (!CollectionUtils.isEmpty(regeoResponseList) && ObjectUtils.allNotNull(regeoResponseList.get(i))) {
        datahubDeviceData = enrichDataWithRegeoResponse(deviceDataPush1007, regeoResponseList.get(i));
      }
      if (ObjectUtils.allNotNull(datahubDeviceData)) {
        datahubDeviceDataList.add(datahubDeviceData);
      }
    }

    return datahubDeviceDataList;
  }

  public static DatahubDeviceData enrichDataWithRegeoResponse(DeviceDataPush1007 deviceDataPush1007, Regeocodes regeocodes) {
    DatahubDeviceData datahubDeviceData;
    try {
      datahubDeviceData = new DatahubDeviceData(deviceDataPush1007, regeocodes.getAddressComponent().getAdcode(), regeocodes.getAddressComponent().getTowncode());
    } catch (Exception e) {
      logger.debug("parse regeo string %s", e.getMessage());
      return null;
    }
    return datahubDeviceData;
  }

  public RegeoResponse getRegeoResponse(RegeoRequest regeoRequest) {
    String regeoResponseString = getRegeoInfo(regeoRequest);
    return convertToRegeo(regeoResponseString);
  }

  /**
   * 逆地理编码
   * @param regeoRequest
   * @return 返回getRegeoInfo
   */
  public static String getRegeoInfo(RegeoRequest regeoRequest) {
    oKHttpUtil = new OKHttpUtil();
    String result = null;
    // 参数校验
    if (!regeoRequest.getKey().isEmpty() && !regeoRequest.getLocations().isEmpty()) {
      try {
        String params = "key=" + regeoRequest.getKey()
                +"&location=" + regeoRequest.getLocations()
                +"&extensions=" + regeoRequest.getExtensions()
                +"&batch=" + regeoRequest.getBatch()
                +"&roadlevel=" + regeoRequest.getRoadlevel();

//				logger.info("高德地图params:" + params);
        result = oKHttpUtil.httpPost(geocodeUrl, params);
//				logger.info("高德地图返回结果:" + result);
      } catch (Exception e) {
        logger.error("高德坐标转换接口请求出错!!!!");
        return null;
      }
    }
    return result;
  }

  public static RegeoResponse convertToRegeo(String convertToRegeoResponseString) {
    RegeoResponse regeoResponse = new RegeoResponse();
    try {
      JsonNode convertToRegeoResponseNode = ObjectMapperUtils.getObjectMapper().readTree(convertToRegeoResponseString);
      if (convertToRegeoResponseNode == null)
        return null;
      else {
        int convertToRegeoStatus = convertToRegeoResponseNode.get(RegeoResponse.STATUS).asInt();
        String convertToRegeoInfoString = convertToRegeoResponseNode.get(RegeoResponse.INFO).asText();
        String convertToRegeoInfoCode = convertToRegeoResponseNode.get(RegeoResponse.INFO_CODE).asText();
        JsonNode convertToRegeoRegeocodes = convertToRegeoResponseNode.get(RegeoResponse.REGEOCODES);

        regeoResponse.setInfo(convertToRegeoInfoString);
        regeoResponse.setInfoCode(convertToRegeoInfoCode);
        regeoResponse.setStatus(convertToRegeoStatus);
        regeoResponse.setRegeocodes(parserArray(convertToRegeoRegeocodes));
        return regeoResponse;
      }
    } catch (IOException e) {
      logger.warn("cannot get coordinate convert string %s", e.getMessage());
      return null;
    }
  }

  private static List<Regeocodes> parserArray(JsonNode jsonNode) {
    if (null!=jsonNode && !jsonNode.isArray()) {
      throw new RuntimeException("json对象不是数组类型");
    }
    List<Regeocodes> result = new ArrayList<>();
    for (JsonNode node : jsonNode) {
      result.add(parserSingle(node));
    }
    return result;
  }

  private static Regeocodes parserSingle(JsonNode node) {
    Regeocodes regeocodes = new Regeocodes();
    JsonNode addressJsonNode = node.get("addressComponent");

    AddressComponent addressComponent = new AddressComponent();
    String adcode = addressJsonNode.get("adcode").asText();
    String towncode = addressJsonNode.get("towncode").asText();

    addressComponent.setAdcode(adcode);
    addressComponent.setTowncode(towncode);

    regeocodes.setAddressComponent(addressComponent);
    return regeocodes;
  }

}