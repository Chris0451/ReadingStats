# 📚 Reading Stats

Applicazione Android sviluppata in **Jetpack Compose** con architettura **MVVM**.  
Il progetto integra **Firebase (Auth + Firestore)** per la gestione degli utenti e dei dati, e la **Google Books API** per la ricerca e i metadati dei libri.  

---

## 🚀 Tecnologie principali

- **Linguaggio**: Kotlin  
- **UI**: Jetpack Compose  
- **Architettura**: MVVM + Dependency Injection  
- **Backend**:
  - Firebase Authentication
  - Firebase Firestore
- **API esterne**: Google Books API  
- **Gestione dipendenze**: Gradle Version Catalog (`libs.versions.toml`)  
- **Iniezione dipendenze**: Hilt/Dagger (moduli in `di/`)  

---

### 🔑 API Key
Il progetto richiede una chiave API di **Google Books**, presente nel file `local.properties`.


### 🛠️ Gradle
- `app/build.gradle.kts` → contiene `buildConfigField` per la Books API Key  
- `gradle/libs.versions.toml` → gestisce tutte le versioni delle dipendenze  
- `settings.gradle.kts` → configurazione moduli  

---

## 📌 Funzionalità principali

- Registrazione/Login con Firebase Authentication  
- Salvataggio e gestione dei dati utente su Firestore  
- Catalogo libri basato su **Google Books API**, con ricerca del titolo e scansione del codice ISBN per ricerca di un libro fisico
- Gestione percentuale di pagine lette di libri in lettura   
- Gestione timer personale per libri in lettura
- Sezione **Amici** per condividere progressi di lettura

---

## 📎 Link utili

- 📖 [Google Books API Documentation](https://developers.google.com/books)  
- 🔥 [Firebase Documentation](https://firebase.google.com/docs)  

---

## 👤 Autori

- **Cristian Di Cintio**
- **Federico Di Giovannangelo**

---

Progetto per il corso di *Programmazione Mobile*, Università Politecnica delle Marche – 2025
