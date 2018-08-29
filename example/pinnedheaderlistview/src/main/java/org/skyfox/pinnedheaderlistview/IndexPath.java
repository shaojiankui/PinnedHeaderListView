package org.skyfox.pinnedheaderlistview;

public class IndexPath {
    public int section;
    public int row;
    public int rawPostion;

    public IndexPath(int _section, int _row, int _rawPostion){
        section = _section;
        row = _row;
        rawPostion = _rawPostion;
    }
}
