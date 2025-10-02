/*    */ package tech.skidonion.obfuscator.inline;
/*    */ import java.time.LocalDateTime;
/*    */ import java.util.*;
/*    */
/*    */

/*    */
/*    */ public class Wrapper {
/*  8 */   private static final Map<Integer, List<String>> DEFAULT_CONSTANT_POOL = new HashMap<>();
/*    */   
/*    */   public static void _debug_addDefaultCloudConstant(String role, String constant) {
/* 11 */     DEFAULT_CONSTANT_POOL.compute(Integer.valueOf(role.hashCode()), (k, v) -> {
/*    */           if (v == null) {
/*    */             v = new ArrayList();
/*    */           }
/*    */           v.add(constant);
/*    */           return v;
/*    */         });
/*    */   }
/*    */   
/*    */   public static Optional<Long> getUserId() {
/* 21 */     return Optional.of(Long.valueOf(Long.MAX_VALUE));
/*    */   }
/*    */   
/*    */   public static Optional<String> getUsername() {
/* 25 */     return Optional.of("development");
/*    */   }
/*    */   
/*    */   public static Optional<String> getNickname() {
/* 29 */     return Optional.of("development");
/*    */   }
/*    */   
/*    */   public static int login(String username, String password, boolean useHashedPassword) {
/* 33 */     return 0;
/*    */   }
/*    */   
/*    */   public static String getVerifyToken() {
/* 37 */     return "";
/*    */   }
/*    */ 
/*    */   
/*    */   public static void setAsSuspected(String reason) {}
/*    */ 
/*    */   
/*    */   public static Optional<String> getCloudConstant(int hash, int index) {
/* 45 */     String key = "phantom-shield-x.cloud-constant." + hash + "." + index;
/*    */     String result;
/* 47 */     if ((result = System.getProperty(key)) != null) {
/* 48 */       return Optional.of(result);
/*    */     }
/*    */     
/* 51 */     List<String> array = DEFAULT_CONSTANT_POOL.get(Integer.valueOf(hash));
/* 52 */     if (array != null && index < array.size() && index >= 0) {
/* 53 */       return Optional.of(array.get(index));
/*    */     }
/* 55 */     return Optional.empty();
/*    */   }
/*    */   
/*    */   public static Optional<LocalDateTime> getExpiredDate(String role) {
/* 59 */     return Optional.of(LocalDateTime.now().plusDays(7L));
/*    */   }
/*    */   
/*    */   public static Map<String, LocalDateTime> getExpiredDates() {
/* 63 */     return Collections.emptyMap();
/*    */   }
/*    */   
/*    */   public static boolean hasRole(String role) {
/* 67 */     return true;
/*    */   }
/*    */ }


/* Location:              D:\project\b\Southside-Public\libraries\phantomshield-inline.jar!\tech\skidonion\obfuscator\inline\Wrapper.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */