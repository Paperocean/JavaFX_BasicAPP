# JavaFX_BasicAPP

## Najważniejsze funkcje projektu

### **MainController** – główny kontroler aplikacji GUI

- **Inicjalizacja interfejsu i obsługa zdarzeń**
    - Inicjuje komponenty GUI, ustawia domyślne wartości, ładuje logo, wyświetla autora.
    - Obsługuje zdarzenia przycisków: wczytywanie, zapisywanie, wykonywanie operacji, obrót, skalowanie obrazu.
    - Dynamicznie aktywuje/dezaktywuje przyciski w zależności od kontekstu (np. czy obraz jest załadowany).
- **Wczytywanie obrazu**
    - Umożliwia wybór pliku JPG/JPEG przez FileChooser.
    - Wczytuje i wyświetla obraz oryginalny, przygotowuje do dalszej obróbki.
    - Obsługuje błędy ładowania i wyświetla komunikaty.
- **Wykonywanie operacji na obrazie**
    - Udostępnia trzy operacje: Negatyw, Progowanie (z możliwością podania progu), Konturowanie (Sobel).
    - Operacje wykonywane są asynchronicznie (Task + ExecutorService), z paskiem postępu i obsługą wyjątków.
    - Po zakończeniu operacji wynik wyświetlany jest w osobnym oknie podglądu.
- **Obrót i skalowanie obrazu**
    - Obrót o 90° w lewo/prawo – asynchronicznie, z zachowaniem jakości.
    - Skalowanie do podanych wymiarów (max 3000x3000 px), z walidacją wejścia i obsługą błędów.
- **Zapisywanie obrazu**
    - Zapisuje przetworzony obraz do pliku JPG w katalogu „Obrazy” użytkownika.
    - Sprawdza poprawność nazwy pliku i obsługuje przypadek nadpisania istniejącego pliku.
- **Komunikaty i logowanie**
    - Wyświetla komunikaty o sukcesie/błędach w statusLabel.
    - Loguje wszystkie istotne zdarzenia (ładowanie, przetwarzanie, zapisywanie, błędy) przez Logger.

---

### **ImageProcessor** – klasa do przetwarzania obrazów

- **Konwersje**
    - `imageToBufferedImage(Image)` – konwersja obrazu JavaFX do BufferedImage.
    - `bufferedImageToImage(BufferedImage)` – konwersja BufferedImage do JavaFX Image.
- **Negatyw**
    - `applyNegative(BufferedImage)` – odwraca kolory obrazu, zachowując kanał alfa.
- **Progowanie**
    - `applyThreshold(BufferedImage, int threshold)` – binaryzuje obraz według progu (0–255), zachowując kanał alfa.
    - `calculateOtsuThreshold(BufferedImage)` – automatycznie wyznacza optymalny próg metodą Otsu.
- **Detekcja krawędzi**
    - `applyEdgeDetection(BufferedImage)` – detekcja krawędzi metodą Sobela, wynik w skali szarości.
- **Obrót**
    - `rotateImage(BufferedImage, int degrees)` – obraca obraz o dowolny kąt, z interpolacją i zachowaniem jakości.
- **Skalowanie**
    - `scaleImage(BufferedImage, int newWidth, int newHeight)` – skalowanie z wysoką jakością interpolacji (max 3000x3000 px).
- **Zapis**
    - `saveImage(BufferedImage, File)` – zapisuje obraz do pliku (obsługa formatów PNG, JPG; automatyczna konwersja do RGB dla JPG).
- **Dodatkowe narzędzia**
    - `copyImage(BufferedImage)` – tworzy głęboką kopię obrazu.
    - `hasAlphaChannel(BufferedImage)` – sprawdza obecność kanału alfa.
    - `convertToGrayscale(BufferedImage)` – konwertuje obraz do skali szarości.

---

### **Logger** – singleton do logowania zdarzeń

- `log(String level, String message)` – zapisuje komunikaty do pliku `application.log` z datą i poziomem (INFO, ERROR).
- Używany w całej aplikacji do śledzenia działania i błędów.

---

### **HelloApplication** – klasa startowa JavaFX

- Inicjuje główne okno aplikacji, ładuje widok FXML i styl CSS, ustawia parametry okna.
- Obsługuje zamknięcie aplikacji i loguje start/stop programu.

---

## **Podsumowanie funkcjonalności**

- **Wczytywanie, wyświetlanie i zapisywanie obrazów**
- **Negatyw, progowanie (ręczne i automatyczne), detekcja krawędzi**
- **Obrót i skalowanie obrazu**
- **Intuicyjny interfejs z obsługą błędów i komunikatami**
- **Logowanie wszystkich operacji i błędów**
