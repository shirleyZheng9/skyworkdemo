package com.iwhalecloud.bote.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillZipReadUtilTest {

  @Test
  void entryPathHasDotDotSegmentDetectsTraversal() {
    assertTrue(SkillZipReadUtil.entryPathHasDotDotSegment("../evil"));
    assertTrue(SkillZipReadUtil.entryPathHasDotDotSegment("skills/foo/../meta.json"));
    assertFalse(SkillZipReadUtil.entryPathHasDotDotSegment("skills/foo/meta.json"));
    assertFalse(SkillZipReadUtil.entryPathHasDotDotSegment("skills/foo..bar/meta.json"));
  }

  @Test
  void copyStreamLimitedRejectsOverflow() {
    byte[] data = new byte[100];
    assertThrows(IOException.class, () -> {
      try (ByteArrayInputStream in = new ByteArrayInputStream(data);
           ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        SkillZipReadUtil.copyStreamLimited(in, out, 50);
      }
    });
  }

  @Test
  void copyStreamLimitedAcceptsWithinLimit() throws IOException {
    byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
    try (ByteArrayInputStream in = new ByteArrayInputStream(data);
         ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      long n = SkillZipReadUtil.copyStreamLimited(in, out, 100);
      assertEquals(5, n);
      assertArrayEquals(data, out.toByteArray());
    }
  }

  @Test
  void walkUtf8ZipFileReadsEntries(@TempDir Path tmp) throws IOException {
    Path zip = tmp.resolve("t.zip");
    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zip), StandardCharsets.UTF_8)) {
      zos.putNextEntry(new ZipEntry("a.txt"));
      zos.write("x".getBytes(StandardCharsets.UTF_8));
      zos.closeEntry();
    }
    String[] holder = new String[1];
    SkillZipReadUtil.walkUtf8ZipFile(zip, (e, in) -> {
      if ("a.txt".equals(e.getName())) {
        holder[0] = new String(SkillZipReadUtil.readEntryBytesLimited(in, 1024), StandardCharsets.UTF_8);
      }
    });
    assertEquals("x", holder[0]);
  }

  @Test
  void walkUtf8StreamClosesAndWalks(@TempDir Path tmp) throws IOException {
    Path zip = tmp.resolve("s.zip");
    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zip), StandardCharsets.UTF_8)) {
      zos.putNextEntry(new ZipEntry("b.txt"));
      zos.write("z".getBytes(StandardCharsets.UTF_8));
      zos.closeEntry();
    }
    byte[] zipBytes = Files.readAllBytes(zip);
    int[] count = {0};
    assertDoesNotThrow(() -> {
      SkillZipReadUtil.walkUtf8(new ByteArrayInputStream(zipBytes), (e, zis) -> {
        if ("b.txt".equals(e.getName())) {
          count[0]++;
        }
      });
    });
    assertEquals(1, count[0]);
  }
}
