package OverallBans.update;

public final class VersionComparator {

    private VersionComparator() {}

    public static final int SILENT = 0;
    public static final int ALERT = 1;
    public static final int DISABLE = 2;

    public static int compare(String current, String latest) {
        int[] cur = parse(current);
        int[] lat = parse(latest);

        int curH = cur[0];
        int curT = cur[1];
        int latH = lat[0];
        int latT = lat[1];

        if (curH > latH) return SILENT;
        if (curH < latH) return DISABLE;
        if (curT > latT) return SILENT;
        if (curT < latT) return ALERT;
        return SILENT;
    }

    private static int[] parse(String version) {
        if (version == null) return new int[]{0, 0, 0};
        String v = version.trim();
        if (v.startsWith("v") || v.startsWith("V")) v = v.substring(1);
        int dashIdx = v.indexOf('-');
        if (dashIdx > 0) v = v.substring(0, dashIdx);
        String[] parts = v.split("\\.");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                result[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) {
                result[i] = 0;
            }
        }
        return result;
    }
}
