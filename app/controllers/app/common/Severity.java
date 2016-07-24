package controllers.app.common;
/**
 * Created by Administrator on 2014/11/3.
 */
public enum Severity {
  INFO(0), WARN(1), ERROR(2), FATAL(3);

  private int level;

  Severity(int level) {
    this.level = level;
  }

  @Override
  public String toString() {
    return "" + level;
  }


  public int getLevel() {
    return level;
  }
}
