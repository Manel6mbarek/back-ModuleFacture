# 🧾 Backend - Module de Facturation

> **Système de facturation complet** développé avec Java Spring Boot, JPA/Hibernate et MySQL  
> Architecture REST API avec base de données relationnelle

[![Java](https://img.shields.io/badge/Java-Spring%20Boot-green.svg)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/Database-MySQL-blue.svg)](https://www.mysql.com/)
[![API](https://img.shields.io/badge/API-REST-orange.svg)](https://restfulapi.net/)

---

## 📋 Table des matières

- [🎯 Aperçu du projet](#-aperçu-du-projet)
- [🏗️ Architecture des données](#️-architecture-des-données)
- [📊 Modèle de données](#-modèle-de-données)
- [🚀 API Endpoints](#-api-endpoints)
- [🔄 Workflow principal](#-workflow-principal)
- [⚙️ Fonctionnalités](#️-fonctionnalités)
- [📈 Statistiques et reporting](#-statistiques-et-reporting)
- [🛡️ Sécurité](#️-sécurité)
- [🔧 Configuration technique](#-configuration-technique)
- [🚀 Démarrage rapide](#-démarrage-rapide)

---

## 🎯 Aperçu du projet

### Technologies utilisées
- **Backend** : Java Spring Boot
- **ORM** : JPA/Hibernate  
- **Base de données** : MySQL
- **Architecture** : API REST
- **Documentation** : PDF automatique avec iText

### Fonctionnalités principales
✅ Gestion complète des clients  
✅ Catalogue de produits par catégories  
✅ Système de commandes avec calculs automatiques  
✅ Facturation avec export PDF  
✅ Dashboard administrateur  
✅ Statistiques et reporting  

---

## 🏗️ Architecture des données

### Modèle relationnel

```
User (1) ←→ (N) Commande
Categorie (1) ←→ (N) Produit  
Commande (1) ←→ (N) LigneCommande
Produit (1) ←→ (N) LigneCommande
Commande (1) ←→ (1) Facture
```

### Entités principales

| Entité | Table | Rôle |
|--------|-------|------|
| **User** | `users` | Gestion des clients |
| **Categorie** | `categories` | Classification produits |
| **Produit** | `produits` | Catalogue |
| **Commande** | `commandes` | Commandes clients |
| **LigneCommande** | `lignes_commande` | Détails commandes |
| **Facture** | `factures` | Facturation |

---

## 📊 Modèle de données

<details>
<summary><strong>👥 Entité User</strong></summary>

**Table** : `users`  
**Rôle** : Gestion des utilisateurs/clients

| Champ | Type | Description |
|-------|------|-------------|
| `id` | Long | Identifiant unique |
| `email` | String | Email unique (validation format) |
| `motDePasse` | String | Mot de passe (min 8 caractères) |
| `nom` | String | Nom |
| `prenom` | String | Prénom |
| `telephone` | String | Numéro de téléphone |
| `adresse` | String | Adresse complète |
| `role` | Enum | Rôle utilisateur |
| `actif` | Boolean | Statut du compte |
| `dateCreation` | LocalDateTime | Date de création |
| `dateModification` | LocalDateTime | Date de modification |

</details>

<details>
<summary><strong>🏷️ Entité Categorie</strong></summary>

**Table** : `categories`  
**Rôle** : Classification des produits

| Champ | Type | Description |
|-------|------|-------------|
| `id` | Long | Identifiant unique |
| `nom` | String | Nom (2-100 caractères) |
| `description` | String | Description optionnelle |
| `actif` | Boolean | Statut |
| `dateCreation` | LocalDateTime | Date de création |
| `dateModification` | LocalDateTime | Date de modification |

</details>

<details>
<summary><strong>📦 Entité Produit</strong></summary>

**Table** : `produits`  
**Rôle** : Catalogue des produits

| Champ | Type | Description |
|-------|------|-------------|
| `id` | Long | Identifiant unique |
| `nom` | String | Nom du produit |
| `description` | String | Description détaillée |
| `prix` | BigDecimal | Prix unitaire |
| `quantiteStock` | Integer | Quantité en stock |
| `seuilAlerte` | Integer | Seuil d'alerte |
| `disponible` | Boolean | Disponibilité |
| `imagePath` | String | Chemin image |
| `categorieId` | Long | ID catégorie |

</details>

<details>
<summary><strong>🛒 Entité Commande</strong></summary>

**Table** : `commandes`  
**Rôle** : Gestion des commandes

| Champ | Type | Description |
|-------|------|-------------|
| `id` | Long | Identifiant unique |
| `numeroCommande` | String | Numéro unique généré |
| `dateCommande` | LocalDateTime | Date de création |
| `statut` | Enum | Statut (EN_ATTENTE, PAYEE, LIVREE, ANNULEE) |
| `sousTotal` | BigDecimal | Sous-total |
| `montantTVA` | BigDecimal | Montant TVA |
| `totalHT` | BigDecimal | Total HT |
| `totalTTC` | BigDecimal | Total TTC |
| `tauxTVA` | BigDecimal | Taux TVA |
| `commentaire` | String | Commentaire optionnel |

</details>

<details>
<summary><strong>📋 Entité LigneCommande</strong></summary>

**Table** : `lignes_commande`  
**Rôle** : Détails des commandes

| Champ | Type | Description |
|-------|------|-------------|
| `id` | Long | Identifiant unique |
| `quantite` | Integer | Quantité commandée |
| `prixUnitaire` | BigDecimal | Prix au moment commande |
| `sousTotal` | BigDecimal | Total calculé |
| `nomProduit` | String | Nom produit (sauvegarde) |

</details>

<details>
<summary><strong>🧾 Entité Facture</strong></summary>

**Table** : `factures`  
**Rôle** : Facturation et paiements

| Champ | Type | Description |
|-------|------|-------------|
| `id` | Long | Identifiant unique |
| `numeroFacture` | String | Numéro unique |
| `dateFacture` | LocalDateTime | Date facture |
| `datePaiement` | LocalDateTime | Date paiement |
| `statut` | Enum | Statut facture |
| `modePaiement` | Enum | Mode paiement |
| `montantHT` | BigDecimal | Montant HT |
| `montantTVA` | BigDecimal | Montant TVA |
| `montantTTC` | BigDecimal | Montant TTC |

</details>

---

## 🚀 API Endpoints

**Base URL** : `http://localhost:8080`

### 👥 Clients - `/api/clients`

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/{id}` | Obtenir un client par ID |
| `POST` | `/` | Créer un nouveau client |
| `PUT` | `/{id}` | Mettre à jour un client |
| `DELETE` | `/{id}` | Supprimer un client |
| `GET` | `/recherche` | Rechercher des clients |
| `GET` | `/email-existe` | Vérifier l'existence d'un email |
| `GET` | `/statistiques/total` | Nombre total de clients |
| `GET` | `/{id}/commandes` | Client avec ses commandes |
| `PATCH` | `/{id}/statut` | Activer/désactiver |
| `GET` | `/recents` | Clients récents |

**Exemple de création client** :
```json
{
  "nom": "Salem",
  "prenom": "Ben Ali",
  "email": "salem.benali@email.com",
  "motDePasse": "123456789",
  "adresse": "123 Rue de Tanyour, Tunis",
  "telephone": "20202020",
  "actif": true
}
```

### 🏷️ Catégories - `/api/categories`

**Exemple de création** :
```json
{
  "nom": "Crêpes",
  "description": "Catégorie regroupant les différentes crêpes",
  "actif": true
}
```

### 📦 Produits - `/api/produits`

**Exemple de création** :
```json
{
  "nom": "Crêpe Nutella",
  "description": "Délicieuse crêpe avec pâte à tartiner Nutella",
  "prix": 16.5,
  "quantiteStock": 15,
  "seuilAlerte": 5,
  "disponible": true,
  "imagePath": "/images/produits/crepe-nutella.png",
  "categorieId": 4
}
```

### 🛒 Commandes - `/api/commandes`

**Création complète avec produits** :
```http
POST /api/commandes/creer-avec-produits
```

```json
{
  "client": {
    "id": 1
  },
  "lignesCommande": [
    {
      "produit": {
        "id": 1
      },
      "quantite": 2
    },
    {
      "produit": {
        "id": 2
      },
      "quantite": 1
    }
  ]
}
```

**Gestion des statuts** :
```http
PATCH /{id}/statut?statut={NOUVEAU_STATUT}
PATCH /{id}/mode-paiement?modePaiement={MODE}
```

#### Statuts disponibles
- `EN_ATTENTE` → `PAYEE` → `LIVREE`
- `ANNULEE` (depuis n'importe quel statut)

#### Modes de paiement
- `ESPECES`
- `CARTE_BANCAIRE`
- `CHEQUE`
- `VIREMENT`

### 🧾 Factures - `/api/factures`

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/{id}/pdf` | Télécharger facture PDF |
| `GET` | `/{id}/pdf/preview` | Prévisualiser PDF |
| `GET` | `/non-traitees` | Factures non traitées |
| `GET` | `/payees-mois` | Factures payées du mois |
| `GET` | `/statistiques` | Statistiques complètes |
| `GET` | `/dashboard` | Tableau de bord admin |
| `PATCH` | `/{id}/marquer-payee?modePaiement={MODE}` | Marquer comme payée |

**Recherche avancée** :
```http
POST /api/factures/recherche-avancee
```

```json
{
  "statut": "PAYEE",
  "dateDebut": "2024-01-01",
  "dateFin": "2024-12-31",
  "clientId": 5
}
```

---

## 🔄 Workflow principal

### 1. 📝 Création d'une commande complète
```http
POST /api/commandes/creer-avec-produits
```
✅ Commande créée avec statut "EN_ATTENTE"  
✅ Facture créée automatiquement  
✅ Calculs automatiques (HT, TVA, TTC)

### 2. ⚙️ Traitement administratif
```http
PATCH /api/commandes/{id}/statut?statut=PAYEE
```
✅ Commande passe en "PAYEE"  
✅ Facture synchronisée automatiquement

### 3. ✅ Finalisation
```http
PATCH /api/commandes/{id}/mode-paiement?modePaiement=CARTE_BANCAIRE
PATCH /api/commandes/{id}/statut?statut=LIVREE
```
✅ Processus complet terminé

---

## ⚙️ Fonctionnalités

### Calculs automatiques
✅ Sous-totaux des lignes de commande  
✅ Total HT, TVA (20%), et TTC  
✅ Synchronisation commande ↔ facture

### Validations
✅ Transitions de statut valides  
✅ Disponibilité des produits  
✅ Formats d'email et contraintes  
✅ Quantités et prix positifs

### Traçabilité
✅ Dates de création/modification automatiques  
✅ Numéros uniques générés automatiquement  
✅ Historique des changements de statut

---

## 📈 Statistiques et reporting

### Dashboard administrateur
```json
{
  "statistiques": {
    "totalFactures": 150,
    "facturesEnAttente": 12,
    "facturesPayees": 135,
    "facturesAnnulees": 3,
    "chiffreAffairesMois": 5420.50,
    "chiffreAffairesTotal": 45300.75
  },
  "facturesNonTraitees": 8,
  "facturesPayeesMois": 23
}
```

### Export PDF
✅ Génération automatique de factures PDF  
✅ En-tête entreprise personnalisable  
✅ Détails complets (client, produits, totaux)  
✅ Statuts et dates importantes

---

## 🛡️ Sécurité

### Contraintes de données
- Email unique et format valide
- Mots de passe minimum 8 caractères
- Prix et quantités positifs
- Transitions de statut cohérentes

### Gestion d'erreurs
```json
{
  "error": "Transition de statut invalide : LIVREE -> EN_ATTENTE"
}
```

---

## 🔧 Configuration technique

### Dépendances principales
- **Spring Boot** (Web, Data JPA, Validation)
- **MySQL Driver**
- **iText** pour génération PDF
- **Jakarta Validation**

### Structure des packages
```
com.facturation.facture/
├── model/           # Entités JPA
├── controller/      # Contrôleurs REST
├── service/         # Logique métier
├── repository/      # Accès aux données
└── DTO/            # Data Transfer Objects
```

---

## 🚀 Démarrage rapide

### Prérequis
- Java 17+
- MySQL 8.0+
- Maven 3.6+

### Installation

1. **Cloner le repository**
   ```bash
   git clone [URL_REPOSITORY]
   cd back-ModuleFacture
   ```

2. **Configuration base de données**
   ```properties
   # application.properties
   spring.datasource.url=jdbc:mysql://localhost:3306/facturation_db
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. **Démarrer l'application**
   ```bash
   mvn spring-boot:run
   ```

4. **Accéder à l'API**
   ```
   http://localhost:8080
   ```

---


**Développé avec ❤️ en Java Spring Boot**
