# back-ModuleFacture
Projet : Backend - Module de facturation
 Technologies : Java Spring Boot, JPA/Hibernate, Mysql
 Architecture : API REST avec base de données relationnelle

🏗️ Architecture des données
Modèle de données principal
Le système de facturation repose sur 5 entités principales interconnectées :
User (Utilisateurs/Clients)
Categorie (Catégories de produits)
Produit (Catalogue des produits)
Commande (Commandes clients)
LigneCommande (Détails des commandes)
Facture (Facturation)
Relations entre entités
User (1) ←→ (N) Commande
Categorie (1) ←→ (N) Produit
Commande (1) ←→ (N) LigneCommande
Produit (1) ←→ (N) LigneCommande
Commande (1) ←→ (1) Facture


📊 Détail des entités
1. Entité User
Table : users
Rôle : Gestion des utilisateurs/clients du système
Champs principaux :
id : Identifiant unique
email : Email unique (validation format)
motDePasse : Mot de passe (min 8 caractères)
nom, prenom : Informations personnelles
telephone, adresse : Coordonnées
role : Énumération des rôles
actif : Statut du compte
dateCreation, dateModification : Traçabilité
Relations :
OneToMany avec Commande
OneToMany avec Notification 

2. Entité Categorie
Table : categories
Rôle : Classification des produits
Champs principaux :
id : Identifiant unique
nom : Nom de la catégorie (2-100 caractères)
description : Description optionnelle
actif : Statut de la catégorie
dateCreation, dateModification : Traçabilité
Relations :
OneToMany avec Produit

3. Entité Produit
Table : produits
Rôle : Catalogue des produits disponibles
Champs principaux :
id : Identifiant unique
nom : Nom du produit
description : Description détaillée
prix : Prix unitaire (BigDecimal)
quantiteStock : Quantité en stock
seuilAlerte : Seuil d'alerte pour le stock
disponible : Disponibilité
imagePath : Chemin vers l'image
dateCreation, dateModification : Traçabilité
Relations :
ManyToOne avec Categorie
OneToMany avec LigneCommande

4. Entité Commande
Table : commandes
Rôle : Gestion des commandes clients
Champs principaux :
id : Identifiant unique
numeroCommande : Numéro unique généré
dateCommande : Date de création
statut : Statut de la commande (énumération)
sousTotal, montantTVA, totalHT, totalTTC : Calculs financiers
tauxTVA : Taux de TVA appliqué
commentaire : Commentaire optionnel
Relations :
ManyToOne avec User (client)
OneToMany avec LigneCommande
OneToOne avec Facture

5. Entité LigneCommande
Table : lignes_commande
Rôle : Détail des produits dans une commande
Champs principaux :
id : Identifiant unique
quantite : Quantité commandée
prixUnitaire : Prix unitaire au moment de la commande
sousTotal : Total calculé automatiquement
nomProduit : Nom du produit (sauvegarde)
Relations :
ManyToOne avec Commande
ManyToOne avec Produit

6. Entité Facture
Table : factures
Rôle : Facturation et suivi des paiements
Champs principaux :
id : Identifiant unique
numeroFacture : Numéro unique
dateFacture, datePaiement : Dates importantes
statut : Statut de la facture
modePaiement : Mode de paiement
montantHT, montantTVA, montantTTC : Montants
commentaire : Commentaire optionnel
Relations :
OneToOne avec Commande

🚀 API Endpoints
Base URL : http://localhost:8080
👥 Users/Clients - /api/clients
Endpoints disponibles :
GET /{id} - Obtenir un client par ID
POST / - Créer un nouveau client
PUT /{id} - Mettre à jour un client
DELETE /{id} - Supprimer un client
GET /recherche - Rechercher des clients
GET /email-existe - Vérifier l'existence d'un email
GET /statistiques/total - Nombre total de clients
GET /{id}/commandes - Obtenir un client avec ses commandes
PATCH /{id}/statut - Activer/désactiver un client
GET /recents - Obtenir les clients récents
Exemple de création :
{
  "nom": "salem",
  "prenom": "benali",
  "email": "salem.benali@email.com",
  "motDePasse": "123456789",
  "adresse": "123 Rue de tanyour, Tunis",
  "telephone": "20202020",
  "actif": true
}

🏷️ Catégories - /api/categories
Exemple de création :
{
  "nom": "Crêpes",
  "description": "Catégorie regroupant les différentes crêpes",
  "actif": true
}

📦 Produits - /api/produits
Exemple de création :
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

🛒 Commandes - /api/commandes
Création complète avec produits :
POST /creer-avec-produits
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

Gestion des statuts :
PATCH /{id}/statut?statut={NOUVEAU_STATUT}
PATCH /{id}/mode-paiement?modePaiement={MODE}
Statuts disponibles :
EN_ATTENTE → PAYEE → LIVREE
ANNULEE (depuis n'importe quel statut)
Modes de paiement :
ESPECES
CARTE_BANCAIRE
CHEQUE
VIREMENT

🧾 Factures - /api/factures
Fonctionnalités PDF :
GET /{id}/pdf - Télécharger facture PDF
GET /{id}/pdf/preview - Prévisualiser PDF
Gestion administrative :
GET /non-traitees - Factures non traitées
GET /payees-mois - Factures payées du mois
GET /statistiques - Statistiques complètes
GET /dashboard - Tableau de bord admin
PATCH /{id}/marquer-payee?modePaiement={MODE} - Marquer comme payée
Recherche avancée :
POST /recherche-avancee
{
  "statut": "PAYEE",
  "dateDebut": "2024-01-01",
  "dateFin": "2024-12-31",
  "clientId": 5
}


🔄 Workflow principal

1. Création d'une commande complète
POST /api/commandes/creer-avec-produits
✅ Commande créée avec statut "EN_ATTENTE"
✅ Facture créée automatiquement
✅ Calculs automatiques (HT, TVA, TTC)

2. Traitement administratif
PATCH /api/commandes/{id}/statut?statut=PAYEE
✅ Commande passe en "PAYEE"
✅ Facture synchronisée automatiquement
3. Finalisation
PATCH /api/commandes/{id}/mode-paiement?modePaiement=CARTE_BANCAIRE
PATCH /api/commandes/{id}/statut?statut=LIVREE
✅ Processus complet terminé

⚙️ Fonctionnalités automatiques
Calculs automatiques :
✅ Sous-totaux des lignes de commande
✅ Total HT, TVA (20%), et TTC
✅ Synchronisation commande ↔ facture
Validations :
✅ Transitions de statut valides
✅ Disponibilité des produits
✅ Formats d'email et contraintes de données
✅ Quantités et prix positifs
Traçabilité :
✅ Dates de création et modification automatiques
✅ Numéros uniques générés automatiquement
✅ Historique des changements de statut

📈 Statistiques et reporting
Dashboard administrateur :
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

Export PDF :
✅ Génération automatique de factures PDF
✅ En-tête entreprise personnalisable
✅ Détails complets (client, produits, totaux)
✅ Statuts et dates importantes

🛡️ Sécurité et validation
Contraintes de données :
Email unique et format valide
Mots de passe minimum 8 caractères
Prix et quantités positifs
Transitions de statut cohérentes
Gestion d'erreurs :
{
  "error": "Transition de statut invalide : LIVREE -> EN_ATTENTE"
}


🔧 Configuration technique
Dépendances principales :
Spring Boot (Web, Data JPA, Validation)
mysql Driver
iText pour génération PDF
Jakarta Validation
Structure des packages :
com.facturation.facture/
├── model/           # Entités JPA
├── controller/      # Contrôleurs REST
├── service/         # Logique métier
├── repository/      # Accès aux données
└── DTO/          # DTO

