/**
 * Generates an Android Binary XML (AXML) buffer for AndroidManifest.xml
 * adhering strictly to the Android OS Resource XML Chunk format (RES_XML_TYPE = 0x00080003).
 *
 * Fixes:
 * 1. 1-to-1 correspondence between first N strings and 0x0180 Resource ID Map chunk
 * 2. Proper boolean constants (0xffffffff for true, 0x00000000 for false)
 * 3. minSdkVersion = 26 (Android 8.0) and targetSdkVersion = 34 (Android 14) to satisfy
 *    Android 14's strict minimum target requirement (targetSdkVersion >= 23).
 * 4. Explicit android:exported="true" on Launcher Activity.
 */

export function buildBinaryAndroidManifest(
  packageName: string = "com.school.manager",
  versionCode: number = 28,
  versionName: string = "2.28.0",
  appName: string = "School Manager"
): Uint8Array {
  // 1. System attributes that map 1:1 with resIds table
  const systemAttrs = [
    { name: "theme", resId: 0x01010000 },
    { name: "label", resId: 0x01010001 },
    { name: "icon", resId: 0x01010002 },
    { name: "name", resId: 0x01010003 },
    { name: "exported", resId: 0x01010010 },
    { name: "screenOrientation", resId: 0x01010012 },
    { name: "configChanges", resId: 0x0101001f },
    { name: "value", resId: 0x01010024 },
    { name: "minSdkVersion", resId: 0x0101020c },
    { name: "versionCode", resId: 0x0101021b },
    { name: "versionName", resId: 0x0101021c },
    { name: "targetSdkVersion", resId: 0x01010270 },
    { name: "allowBackup", resId: 0x01010280 },
    { name: "supportsRtl", resId: 0x010103af }
  ];

  // The first N strings in the string pool MUST be exactly the system attribute names!
  const strings: string[] = systemAttrs.map(a => a.name);

  // Additional elements, namespaces, and attribute values
  const nonAttrStrings = [
    // Namespaces & prefix
    "http://schemas.android.com/apk/res/android",
    "android",
    // Elements
    "manifest",
    "uses-sdk",
    "uses-permission",
    "application",
    "activity",
    "intent-filter",
    "action",
    "category",
    "meta-data",
    // Non-system attributes
    "package",
    // Attribute values
    packageName,
    versionName,
    appName,
    "android.permission.INTERNET",
    "android.permission.ACCESS_NETWORK_STATE",
    "android.permission.CAMERA",
    "android.permission.VIBRATE",
    `${packageName}.MainActivity`,
    "android.intent.action.MAIN",
    "android.intent.category.LAUNCHER",
    "portrait",
    "orientation|keyboardHidden|screenSize",
    "@android:style/Theme.NoTitleBar.Fullscreen",
    "@drawable/ic_launcher",
    "socialman121@gmail.com"
  ];

  for (const s of nonAttrStrings) {
    if (!strings.includes(s)) {
      strings.push(s);
    }
  }

  const strIndex = (s: string) => strings.indexOf(s);

  function u16(val: number): number[] {
    return [val & 0xff, (val >> 8) & 0xff];
  }

  function u32(val: number): number[] {
    return [
      val & 0xff,
      (val >> 8) & 0xff,
      (val >> 16) & 0xff,
      (val >> 24) & 0xff
    ];
  }

  // 1. Build String Pool Chunk (0x0001)
  const stringOffsets: number[] = [];
  const stringBytes: number[] = [];

  for (let i = 0; i < strings.length; i++) {
    stringOffsets.push(stringBytes.length);
    const s = strings[i];
    // UTF-16 encoding: character count as uint16, then characters as uint16, then 2 null bytes
    stringBytes.push(...u16(s.length));
    for (let j = 0; j < s.length; j++) {
      stringBytes.push(...u16(s.charCodeAt(j)));
    }
    stringBytes.push(0x00, 0x00);
  }

  // 4-byte align stringBytes
  while (stringBytes.length % 4 !== 0) {
    stringBytes.push(0x00);
  }

  const stringPoolHeaderSize = 28;
  const stringOffsetsSize = stringOffsets.length * 4;
  const stringsStart = stringPoolHeaderSize + stringOffsetsSize;
  const stringPoolChunkSize = stringsStart + stringBytes.length;

  const stringPoolChunk: number[] = [
    ...u16(0x0001), // RES_STRING_POOL_TYPE
    ...u16(stringPoolHeaderSize),
    ...u32(stringPoolChunkSize),
    ...u32(strings.length), // stringCount
    ...u32(0), // styleCount
    ...u32(0), // flags (0 = UTF-16)
    ...u32(stringsStart),
    ...u32(0) // stylesStart
  ];

  for (const offset of stringOffsets) {
    stringPoolChunk.push(...u32(offset));
  }
  stringPoolChunk.push(...stringBytes);

  // 2. Resource IDs Map Chunk (0x0180)
  // Maps 1:1 with the first systemAttrs.length strings!
  const resIdsChunkSize = 8 + systemAttrs.length * 4;
  const resIdsChunk: number[] = [
    ...u16(0x0180),
    ...u16(8),
    ...u32(resIdsChunkSize)
  ];
  for (const a of systemAttrs) {
    resIdsChunk.push(...u32(a.resId));
  }

  const uriIdx = strIndex("http://schemas.android.com/apk/res/android");
  const prefixIdx = strIndex("android");

  // XML Namespace Start Chunk (0x0100)
  const nsStartChunk: number[] = [
    ...u16(0x0100),
    ...u16(16),
    ...u32(24),
    ...u32(1), // line number
    ...u32(0xffffffff), // comment
    ...u32(prefixIdx),
    ...u32(uriIdx)
  ];

  // Helper for Start Element Chunk (0x0102) & End Element Chunk (0x0103)
  interface AttrDef {
    ns: number;
    name: number;
    valStr: number;
    type: number;
    data: number;
  }

  function createElementChunks(
    nameIdx: number,
    attrs: AttrDef[] = [],
    childrenChunks: number[][] = []
  ): number[] {
    const attrSize = 20;
    const startChunkSize = 16 + 20 + attrs.length * attrSize;
    const start: number[] = [
      ...u16(0x0102), // RES_XML_START_ELEMENT_TYPE
      ...u16(16),
      ...u32(startChunkSize),
      ...u32(1), // line
      ...u32(0xffffffff), // comment
      ...u32(0xffffffff), // ns
      ...u32(nameIdx),
      ...u16(0x0014), // attrStart (offset 20 from attrExt)
      ...u16(attrSize),
      ...u16(attrs.length),
      ...u16(0), // idIndex
      ...u16(0), // classIndex
      ...u16(0)  // styleIndex
    ];

    for (const a of attrs) {
      start.push(
        ...u32(a.ns),
        ...u32(a.name),
        ...u32(a.valStr),
        ...u16(8), // typedValue size
        0x00, // res0
        a.type, // dataType (0x03=string, 0x10=int_dec, 0x12=boolean)
        ...u32(a.data)
      );
    }

    const end: number[] = [
      ...u16(0x0103), // RES_XML_END_ELEMENT_TYPE
      ...u16(16),
      ...u32(24),
      ...u32(1),
      ...u32(0xffffffff),
      ...u32(0xffffffff),
      ...u32(nameIdx)
    ];

    const result = [...start];
    for (const child of childrenChunks) {
      result.push(...child);
    }
    result.push(...end);
    return result;
  }

  // <action android:name="android.intent.action.MAIN" />
  const actionChunk = createElementChunks(
    strIndex("action"),
    [
      {
        ns: uriIdx,
        name: strIndex("name"),
        valStr: strIndex("android.intent.action.MAIN"),
        type: 0x03,
        data: strIndex("android.intent.action.MAIN")
      }
    ]
  );

  // <category android:name="android.intent.category.LAUNCHER" />
  const categoryChunk = createElementChunks(
    strIndex("category"),
    [
      {
        ns: uriIdx,
        name: strIndex("name"),
        valStr: strIndex("android.intent.category.LAUNCHER"),
        type: 0x03,
        data: strIndex("android.intent.category.LAUNCHER")
      }
    ]
  );

  // <intent-filter>
  const intentFilterChunk = createElementChunks(
    strIndex("intent-filter"),
    [],
    [actionChunk, categoryChunk]
  );

  // <activity android:name="MainActivity" android:exported="true" android:screenOrientation="portrait">
  const activityChunk = createElementChunks(
    strIndex("activity"),
    [
      {
        ns: uriIdx,
        name: strIndex("name"),
        valStr: strIndex(`${packageName}.MainActivity`),
        type: 0x03,
        data: strIndex(`${packageName}.MainActivity`)
      },
      {
        ns: uriIdx,
        name: strIndex("exported"),
        valStr: 0xffffffff,
        type: 0x12, // boolean
        data: 0xffffffff // TRUE in Android Res_value (all bits 1)
      },
      {
        ns: uriIdx,
        name: strIndex("screenOrientation"),
        valStr: strIndex("portrait"),
        type: 0x03,
        data: strIndex("portrait")
      }
    ],
    [intentFilterChunk]
  );

  // <application android:label="..." android:allowBackup="true" android:supportsRtl="true" ...>
  const applicationChunk = createElementChunks(
    strIndex("application"),
    [
      {
        ns: uriIdx,
        name: strIndex("label"),
        valStr: strIndex(appName),
        type: 0x03,
        data: strIndex(appName)
      },
      {
        ns: uriIdx,
        name: strIndex("allowBackup"),
        valStr: 0xffffffff,
        type: 0x12,
        data: 0xffffffff // TRUE
      },
      {
        ns: uriIdx,
        name: strIndex("supportsRtl"),
        valStr: 0xffffffff,
        type: 0x12,
        data: 0xffffffff // TRUE
      }
    ],
    [activityChunk]
  );

  // <uses-sdk android:minSdkVersion="26" android:targetSdkVersion="34" />
  // Android 14+ requires targetSdkVersion >= 23 or package installer blocks install!
  const usesSdkChunk = createElementChunks(
    strIndex("uses-sdk"),
    [
      {
        ns: uriIdx,
        name: strIndex("minSdkVersion"),
        valStr: 0xffffffff,
        type: 0x10, // integer
        data: 26 // Android 8.0 Oreo
      },
      {
        ns: uriIdx,
        name: strIndex("targetSdkVersion"),
        valStr: 0xffffffff,
        type: 0x10,
        data: 34 // Android 14
      }
    ]
  );

  // Permissions: INTERNET & ACCESS_NETWORK_STATE
  const permInternet = createElementChunks(
    strIndex("uses-permission"),
    [
      {
        ns: uriIdx,
        name: strIndex("name"),
        valStr: strIndex("android.permission.INTERNET"),
        type: 0x03,
        data: strIndex("android.permission.INTERNET")
      }
    ]
  );

  const permNetwork = createElementChunks(
    strIndex("uses-permission"),
    [
      {
        ns: uriIdx,
        name: strIndex("name"),
        valStr: strIndex("android.permission.ACCESS_NETWORK_STATE"),
        type: 0x03,
        data: strIndex("android.permission.ACCESS_NETWORK_STATE")
      }
    ]
  );

  // <manifest package="..." versionCode="..." versionName="...">
  // package attribute has NO namespace (0xffffffff)
  const manifestElement = createElementChunks(
    strIndex("manifest"),
    [
      {
        ns: 0xffffffff,
        name: strIndex("package"),
        valStr: strIndex(packageName),
        type: 0x03,
        data: strIndex(packageName)
      },
      {
        ns: uriIdx,
        name: strIndex("versionCode"),
        valStr: 0xffffffff,
        type: 0x10,
        data: versionCode
      },
      {
        ns: uriIdx,
        name: strIndex("versionName"),
        valStr: strIndex(versionName),
        type: 0x03,
        data: strIndex(versionName)
      }
    ],
    [usesSdkChunk, permInternet, permNetwork, applicationChunk]
  );

  // XML Namespace End Chunk (0x0101)
  const nsEndChunk: number[] = [
    ...u16(0x0101),
    ...u16(16),
    ...u32(24),
    ...u32(1),
    ...u32(0xffffffff),
    ...u32(prefixIdx),
    ...u32(uriIdx)
  ];

  // Combine everything
  const bodyBytes = [
    ...stringPoolChunk,
    ...resIdsChunk,
    ...nsStartChunk,
    ...manifestElement,
    ...nsEndChunk
  ];

  const totalFileSize = 8 + bodyBytes.length;

  const header: number[] = [
    ...u16(0x0003), // RES_XML_TYPE
    ...u16(8),
    ...u32(totalFileSize)
  ];

  return new Uint8Array([...header, ...bodyBytes]);
}
