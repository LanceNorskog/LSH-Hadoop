package org.apache.mahout.math;
import org.apache.mahout.math.AbstractMatrix;

/*
 * A misnomer: the values and behavior are read-only, but the bindings are read-write.
 * Or should they be read-only as well?
 */

public abstract class ReadOnlyMatrix extends AbstractMatrix {

  public void cloneBindings(RandomMatrix clone) {
    clone.setRowLabelBindings(rowLabelBindings);
    clone.setColumnLabelBindings(columnLabelBindings);
  }

}
