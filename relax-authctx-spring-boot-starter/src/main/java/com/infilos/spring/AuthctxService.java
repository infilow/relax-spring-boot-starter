package com.infilos.spring;

import java.util.Map;
import java.util.Optional;

public interface AuthctxService<T> {

  Optional<T> findUser(Map<String,String> realAttributes);
  
  Optional<T> buildUser(Map<String,String> mockAttributes);
}
