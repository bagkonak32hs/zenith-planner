# Android Uygulama Geliştirme Master Promptu: Premium Dijital Ajanda

**Hedef:** Mevcut premium dijital ajanda tasarımlarını (kapak, günlük planlayıcı, hedef takip, alışkanlık takip, motivasyon sayfası) temel alarak, uçtan uca tam ve eksiksiz bir Android mobil uygulaması geliştirmek için detaylı bir yol haritası, mimari önerileri, UI/UX bileşenleri ve teknoloji yığını tavsiyeleri sun.

## 1. Proje Genel Bakışı

**Uygulama Adı:** Premium Dijital Ajanda (Önerilen: "Zenith Planner" veya "Aura Agenda")
**Amaç:** Kullanıcıların günlük, aylık ve uzun vadeli hedeflerini, alışkanlıklarını ve motivasyonlarını minimalist lüks bir arayüzle dijital ortamda yönetmelerini sağlamak.
**Hedef Kitle:** Profesyoneller, öğrenciler ve kişisel gelişimine önem veren, estetik ve fonksiyonelliği bir arada arayan Android kullanıcıları.

## 2. Tasarım ve UI/UX Prensipleri

Uygulamanın tasarımı, daha önce oluşturulan görsellerdeki estetiği birebir yansıtmalıdır. Anahtar tasarım prensipleri:

*   **Minimalist Lüks Stil:** Temiz çizgiler, gereksiz öğelerden arındırılmış, sofistike bir görünüm.
*   **Renk Paleti:** Siyah, krem ve altın tonları hakim olmalı. Altın detaylar vurgu amaçlı kullanılmalı.
*   **Modern Temiz Düzen:** Apple UI (iOS) tasarım prensiplerinden ilham alan, sezgisel ve kullanıcı dostu bir arayüz.
*   **Zarif Tipografi:** Okunabilirliği yüksek, modern ve şık fontlar kullanılmalı.
*   **Yumuşak Gölgeler:** UI öğelerine derinlik ve premium his katmak için hafif ve doğal gölgeler.
*   **Yüksek Çözünürlük ve Detay:** Tüm grafik öğeleri ve ikonlar 8K çözünürlükte tasarlanmış gibi keskin ve detaylı olmalı.
*   **iPad/GoodNotes Uyumluluğu:** Dijital ajanda kullanıcılarının alışkın olduğu düzen ve etkileşim biçimleri Android'e adapte edilmeli.

## 3. Temel Özellikler ve Modüller

Uygulama aşağıdaki ana modülleri içermelidir. Her bir modül, ilgili ajanda sayfasının işlevselliğini ve tasarımını yansıtmalıdır:

### 3.1. Ana Ekran / Takvim Modülü

*   **Görsel:** `2026_digital_planner_cover.png` görselindeki takvim görünümü temel alınmalı.
*   **İşlevsellik:** Aylık takvim görünümü, günlere dokunarak günlük planlayıcıya geçiş.
*   **Navigasyon:** Aylık ve yıllık görünümler arasında kolay geçiş, yan menüden diğer modüllere erişim (Tasks, Notes, Finance, Goals, Habits vb.).
*   **Tasarım:** Sol tarafta sabit navigasyon menüsü, sağda takvim ve seçilen günün öncelikleri/notları.

### 3.2. Günlük Planlayıcı Modülü

*   **Görsel:** `daily_planner_layout.png` görselindeki düzen kullanılmalı.
*   **İşlevsellik:**
    *   **Top 3 Tasks:** Kullanıcının en önemli 3 görevini belirlemesi ve tamamlandığında işaretlemesi.
    *   **Hourly Schedule:** Saatlik zaman çizelgesi (örn. 6 AM - 10 PM) ile etkinlik girişi.
    *   **Notes:** Serbest biçimli notlar için alan.
    *   **Self-Reflection Question:** Günlük motivasyon ve düşünme için bir soru alanı.
    *   **Habit Tracker Icons:** Günlük alışkanlıkları işaretlemek için küçük ikonlar (su, egzersiz, meditasyon vb.).
*   **Tasarım:** Temiz, bölümlere ayrılmış UI, yüksek okunabilirlik.

### 3.3. Hedef Takip Modülü

*   **Görsel:** `goal_tracking_page.png` görselindeki düzen kullanılmalı.
*   **İşlevsellik:**
    *   **Monthly Goals:** Aylık hedefleri belirleme ve ilerlemeyi takip etme.
    *   **90-Day Plan:** Üç aylık planlama için bölümler.
    *   **Life Goals Categories:** Finans, Sağlık, İş, Kişisel gibi kategoriler altında uzun vadeli hedefler.
    *   **Progress Tracking:** Hedeflerin tamamlanma durumunu görsel olarak gösterme (örn. ilerleme çubukları).
*   **Tasarım:** Temiz grid sistemi, zarif altın vurgular, üretkenlik odaklı.

### 3.4. Alışkanlık Takip Modülü

*   **Görsel:** `habit_tracker_page.png` görselindeki düzen kullanılmalı.
*   **İşlevsellik:**
    *   **Grid Layout with Checkboxes:** Belirlenen alışkanlıkları (su, fitness, çalışma, uyku vb.) günlük olarak işaretleme.
    *   **Kategori Bazlı Takip:** Alışkanlıkları kategorilere ayırma.
    *   **İstatistikler:** Alışkanlık tamamlama oranlarını gösteren basit grafikler veya yüzdeler.
*   **Tasarım:** Minimalist UI, yumuşak nötr renkler, düzenli ve görsel olarak tatmin edici.

### 3.5. Motivasyon Sayfası Modülü

*   **Görsel:** `motivational_planner_page.png` görselindeki düzen kullanılmalı.
*   **İşlevsellik:**
    *   **Inspirational Quote:** Merkezde büyük, değiştirilebilir veya günlük olarak güncellenen ilham verici bir alıntı.
    *   **Kişiselleştirme:** Kullanıcının kendi alıntılarını eklemesine izin verme.
*   **Tasarım:** Siyah arka plan, altın metin, yüksek kontrast, sakinleştirici ve güçlü görsel stil.

## 4. Teknik Mimari ve Teknoloji Yığını Önerileri

*   **Programlama Dili:** Kotlin
*   **UI Çerçevesi:** Jetpack Compose (Modern Android UI geliştirme için)
*   **Mimari Desen:** MVVM (Model-View-ViewModel) veya MVI (Model-View-Intent) (Temiz kod, test edilebilirlik ve ölçeklenebilirlik için)
*   **Veri Depolama:**
    *   **Yerel Veritabanı:** Room Persistence Library (SQLite üzerinde soyutlama, hızlı ve güvenilir yerel veri depolama için)
    *   **Veri Senkronizasyonu (Opsiyonel):** Firebase Firestore veya başka bir bulut tabanlı çözüm (kullanıcı isterse cihazlar arası senkronizasyon için).
*   **Bağımlılık Enjeksiyonu:** Hilt (Daha kolay bağımlılık yönetimi ve test edilebilirlik için)
*   **Asenkron İşlemler:** Kotlin Coroutines ve Flow (Daha temiz ve verimli asenkron programlama için)
*   **Navigasyon:** Jetpack Navigation Component
*   **Grafikler/İkonlar:** Vektör çizilebilirler (SVG) veya yüksek çözünürlüklü PNG'ler (tasarımın keskinliğini korumak için).

## 5. Kullanıcı Etkileşimleri ve Deneyimi

*   **Giriş/Kayıt:** Basit ve güvenli bir giriş/kayıt akışı (e-posta/şifre, Google/Apple ile giriş).
*   **Veri Girişi:** Kullanıcı dostu formlar, tarih/saat seçiciler, metin giriş alanları.
*   **İşaretleme/Tamamlama:** Görevler ve alışkanlıklar için kolayca işaretlenebilir onay kutuları.
*   **Bildirimler:** Hatırlatıcılar ve motivasyonel mesajlar için özelleştirilebilir bildirimler.
*   **Widget'lar:** Ana ekran widget'ları (örn. günlük görevler, alışkanlıklar) için destek.

## 6. Monetizasyon Stratejileri (Opsiyonel)

*   **Tek Seferlik Satın Alma:** Uygulamanın tüm özelliklerine ömür boyu erişim.
*   **Abonelik Modeli:** Premium özellikler (bulut senkronizasyonu, gelişmiş istatistikler, özel temalar) için aylık/yıllık abonelik.
*   **Uygulama İçi Satın Almalar:** Ek tema paketleri, ikon setleri veya özel fontlar.

## 7. Geliştirme Yol Haritası ve Teslimatlar

AI modeli, yukarıdaki bilgileri kullanarak aşağıdaki çıktıları sağlamalıdır:

1.  **Detaylı Mimari Tasarım:** Uygulamanın modüler yapısı, veri akışı ve bileşenler arası ilişkileri gösteren bir açıklama.
2.  **UI/UX Bileşen Listesi:** Her bir ekran için gerekli olan Jetpack Compose bileşenlerinin listesi ve nasıl kullanılacağına dair örnekler.
3.  **Veritabanı Şeması:** Room veritabanı için entity'ler, DAO'lar ve ilişkileri içeren bir şema önerisi.
4.  **Temel Kod Yapısı:** Proje dizin yapısı, ana aktivite/composable'lar için pseudo-code veya başlangıç şablonları.
5.  **API Entegrasyonu (Senkronizasyon için):** Eğer senkronizasyon öneriliyorsa, temel API gereksinimleri ve entegrasyon adımları.
6.  **Test Stratejileri:** Birim testleri, UI testleri ve entegrasyon testleri için öneriler.
7.  **Geliştirme Aşamaları:** Projenin MVP (Minimum Viable Product) ve sonraki aşamalarını içeren, tahmini sürelerle birlikte bir geliştirme planı.
8.  **Potansiyel Zorluklar ve Çözüm Önerileri:** Geliştirme sürecinde karşılaşılabilecek olası sorunlar ve bunlara yönelik çözümler.

Bu prompt, AI modelinin kapsamlı ve uygulanabilir bir Android uygulama geliştirme planı oluşturması için yeterli detayı sağlamalıdır. Uygulama, kullanıcının dijital ajanda vizyonunu mobil platforma taşıyarak, estetik ve işlevselliği bir araya getiren premium bir deneyim sunmalıdır.
