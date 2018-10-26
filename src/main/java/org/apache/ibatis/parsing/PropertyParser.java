/**
 *    Copyright 2009-2016 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.parsing;

import java.util.Properties;

/**
 * @author Clinton Begin
 * @author Kazuki Shimizu
 */
public class PropertyParser {

  private static final String KEY_PREFIX = "org.apache.ibatis.parsing.PropertyParser.";
  /**
   * The special property key that indicate whether enable a default value on placeholder.
   * <p>
   *   The default value is {@code false} (indicate disable a default value on placeholder)
   *   If you specify the {@code true}, you can specify key and default value on placeholder (e.g. {@code ${db.username:postgres}}).
   * </p>在myabtis-config.xml中properties节点下配置是否开启默认值功能的对应配置项
   * @since 3.4.2
   */
  public static final String KEY_ENABLE_DEFAULT_VALUE = KEY_PREFIX + "enable-default-value";

  /**
   * The special property key that specify a separator for key and default value on placeholder.
   * <p>
   *   The default separator is {@code ":"}.
   * </p>配置占位符与默认值之间的默认分隔符的对应配置项
   * @since 3.4.2
   */
  public static final String KEY_DEFAULT_VALUE_SEPARATOR = KEY_PREFIX + "default-value-separator";

  private static final String ENABLE_DEFAULT_VALUE = "false";//默认情况下，关闭默认值功能
  private static final String DEFAULT_VALUE_SEPARATOR = ":";//默认分隔符

  private PropertyParser() {
    // Prevent Instantiation
  }

  public static String parse(String string, Properties variables) {
    VariableTokenHandler handler = new VariableTokenHandler(variables);
    // 创建GenericTokenParser对象，并指定其处理的占位符格式为"${}"
    GenericTokenParser parser = new GenericTokenParser("${", "}", handler);
    // 将默认值的处理委托给GenericTokenParser.parse方法
    return parser.parse(string);
  }

  private static class VariableTokenHandler implements TokenHandler {
    private final Properties variables; // <properties>节点下定义的键值对，用于替换占位符
    private final boolean enableDefaultValue;// 是否支持占位符中使用默认值的功能
    private final String defaultValueSeparator;// 指定占位符与默认值之间的分隔符

    private VariableTokenHandler(Properties variables) {
      this.variables = variables;
      this.enableDefaultValue = Boolean.parseBoolean(getPropertyValue(KEY_ENABLE_DEFAULT_VALUE, ENABLE_DEFAULT_VALUE));
      this.defaultValueSeparator = getPropertyValue(KEY_DEFAULT_VALUE_SEPARATOR, DEFAULT_VALUE_SEPARATOR);
    }

    private String getPropertyValue(String key, String defaultValue) {
      return (variables == null) ? defaultValue : variables.getProperty(key, defaultValue);
    }

    @Override
    public String handleToken(String content) {
      if (variables != null) {
        String key = content;
        if (enableDefaultValue) {// 检测是否支持占位符中使用默认值的功能
          final int separatorIndex = content.indexOf(defaultValueSeparator);//查找分隔符
          String defaultValue = null;
          if (separatorIndex >= 0) {
            key = content.substring(0, separatorIndex);// 获取占位符的名称
            defaultValue = content.substring(separatorIndex + defaultValueSeparator.length());//获取默认值
          }
          if (defaultValue != null) {// 查找指定的占位符
            return variables.getProperty(key, defaultValue);
          }
        }
        if (variables.containsKey(key)) {// 不支持默认值的功能，直接查找
          return variables.getProperty(key);
        }
      }
      return "${" + content + "}";
    }
  }

}
