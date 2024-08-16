package com.reactor.webdav.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class Propfind {
  String path;
  String depth; // "0", "1", or "infinity"
  List<String> props;
  boolean allProp;
  boolean propName;
}
