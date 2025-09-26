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
Il progetto richiede una chiave API di **Google Books**.  
Aggiungila nel file `local.properties`:


### 🛠️ Gradle
- `app/build.gradle.kts` → contiene `buildConfigField` per la Books API Key  
- `gradle/libs.versions.toml` → gestisce tutte le versioni delle dipendenze  
- `settings.gradle.kts` → configurazione moduli  

---

## 📌 Funzionalità principali

- Registrazione/Login con Firebase Authentication  
- Salvataggio e gestione dei dati utente su Firestore  
- Catalogo libri basato su **Google Books API**  
- Creazione di **scaffali personalizzati** per organizzare i libri  
- Possibilità di lasciare recensioni e gestire lo stato di lettura  
- Sezione **Amici** per condividere progressi di lettura  

---

## 📎 Link utili

- 🔗 [Repository GitHub](https://github.com/Chris0451/Reading_Stats)
- 📖 [Google Books API Documentation](https://developers.google.com/books)  
- 🔥 [Firebase Documentation](https://firebase.google.com/docs)  

---

## 👤 Autori

- **Cristian Di Cintio**
- **Federico Di Giovannangelo**

---

Progetto per il corso di *Programmazione Mobile*, Università Politecnica delle Marche – 2025
