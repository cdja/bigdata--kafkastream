package com.chedaojunan.report.model;

import java.io.IOException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Evaluation {

  private static final String EXPEDITE = "expedite";
  private static final String CONGESTED = "congested";
  private static final String BLOCKED = "blocked";
  private static final String UNKNOWN = "unknown";
  private static final String STATUS = "status";
  private static final String DESCRIPTION = "description";

  @JsonProperty(EXPEDITE)
  private Object expedite; // 畅通所占百分比

  @JsonProperty(CONGESTED)
  private Object congested; // 缓行所占百分比

  @JsonProperty(BLOCKED)
  private Object blocked; // 拥堵所占百分比

  @JsonProperty(UNKNOWN)
  private Object unknown; // 未知路段所占百分比

  @JsonProperty(STATUS)
  private Object status; // 路况

  @JsonProperty(DESCRIPTION)
  private Object description; // 道路描述

  public Object getExpedite() {
    return expedite;
  }

  public void setExpedite(Object expedite) {
    this.expedite = expedite;
  }

  public Object getCongested() {
    return congested;
  }

  public void setCongested(Object congested) {
    this.congested = congested;
  }

  public Object getBlocked() {
    return blocked;
  }

  public void setBlocked(Object blocked) {
    this.blocked = blocked;
  }

  public Object getUnknown() {
    return unknown;
  }

  public void setUnknown(Object unknown) {
    this.unknown = unknown;
  }

  public Object getStatus() {
    return status;
  }

  public void setStatus(Object status) {
    this.status = status;
  }

  public Object getDescription() {
    return description;
  }

  public void setDescription(Object description) {
    this.description = description;
  }

  @Override
  public String toString() {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsString(this);
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(status)
        .append(expedite)
        .append(congested)
        .append(blocked)
        .append(unknown)
        .append(description).toHashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof Evaluation) == false) {
      return false;
    }
    Evaluation rhs = ((Evaluation) other);
    return new EqualsBuilder()
        .append(status, rhs.status)
        .append(expedite, rhs.expedite)
        .append(congested, rhs.congested)
        .append(description, rhs.description)
        .append(blocked, rhs.blocked)
        .append(unknown, rhs.unknown)
        .isEquals();
  }
}
