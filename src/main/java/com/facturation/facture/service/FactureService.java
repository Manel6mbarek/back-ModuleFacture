package com.facturation.facture.service;

import com.facturation.facture.dto.FactureDTO;
import com.facturation.facture.model.*;
import com.facturation.facture.model.enums.StatutFacture;
import com.facturation.facture.model.enums.ModePaiement;
import com.facturation.facture.repository.FactureRepository;
import com.facturation.facture.repository.CommandeRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class FactureService {

    private final FactureRepository factureRepository;
    private final CommandeRepository commandeRepository;

    @Autowired
    public FactureService(FactureRepository factureRepository, CommandeRepository commandeRepository) {
        this.factureRepository = factureRepository;
        this.commandeRepository = commandeRepository;
    }

    /**
     * Générer et exporter une facture en PDF
     */
    public ByteArrayResource exporterFacturePDF(Long factureId) {
        Optional<Facture> factureOpt = factureRepository.findById(factureId);
        if (factureOpt.isEmpty()) {
            throw new RuntimeException("Facture non trouvée avec l'ID : " + factureId);
        }

        Facture facture = factureOpt.get();
        byte[] pdfBytes = genererPDFFacture(facture);

        return new ByteArrayResource(pdfBytes);
    }

    /**
     * Générer le PDF d'une facture
     */
    private byte[] genererPDFFacture(Facture facture) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            document.open();

            // Styles de police
            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, Color.BLACK);
            Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);
            Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
            Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);

            // En-tête de la facture
            Paragraph title = new Paragraph("FACTURE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            // Informations de l'entreprise (à personnaliser)
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1, 1});

            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.addElement(new Paragraph("VOTRE ENTREPRISE", headerFont));
            leftCell.addElement(new Paragraph("123 Rue de la Facturation", normalFont));
            leftCell.addElement(new Paragraph("75001 Paris, France", normalFont));
            leftCell.addElement(new Paragraph("Tél: 01 23 45 67 89", normalFont));
            leftCell.addElement(new Paragraph("Email: contact@entreprise.com", normalFont));

            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            rightCell.addElement(new Paragraph("Numéro: " + facture.getNumeroFacture(), boldFont));
            rightCell.addElement(new Paragraph("Date: " + facture.getDateFacture().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), normalFont));
            rightCell.addElement(new Paragraph("Statut: " + getStatutLibelle(facture.getStatut()), boldFont));
            if (facture.getDatePaiement() != null) {
                rightCell.addElement(new Paragraph("Payée le: " + facture.getDatePaiement().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), normalFont));
            }

            headerTable.addCell(leftCell);
            headerTable.addCell(rightCell);
            document.add(headerTable);
            document.add(new Paragraph(" ")); // Espace

            // Informations client
            Paragraph clientTitle = new Paragraph("FACTURÉ À:", headerFont);
            document.add(clientTitle);

            PdfPTable clientTable = new PdfPTable(1);
            clientTable.setWidthPercentage(50);
            clientTable.setHorizontalAlignment(Element.ALIGN_LEFT);

            PdfPCell clientCell = new PdfPCell();
            clientCell.setBorder(Rectangle.BOX);
            clientCell.setPadding(10f);
            clientCell.addElement(new Paragraph(facture.getNomClient() != null ? facture.getNomClient() : "Client", boldFont));

            // Ajouter d'autres infos client si disponibles
            if (facture.getCommande() != null && facture.getCommande().getClient() != null) {
                User client = facture.getCommande().getClient();
                if (client.getEmail() != null) {
                    clientCell.addElement(new Paragraph("Email: " + client.getEmail(), normalFont));
                }
                // Ajouter adresse, téléphone si disponibles dans votre modèle User
            }

            clientTable.addCell(clientCell);
            document.add(clientTable);
            document.add(new Paragraph(" ")); // Espace

            // Détails de la commande
            if (facture.getCommande() != null) {
                Paragraph commandeTitle = new Paragraph("DÉTAILS DE LA COMMANDE:", headerFont);
                document.add(commandeTitle);

                Paragraph commandeInfo = new Paragraph("Commande N°: " + facture.getCommande().getNumeroCommande(), normalFont);
                commandeInfo.add(new Paragraph("Date commande: " + facture.getCommande().getDateCommande().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), normalFont));
                commandeInfo.add(new Paragraph("Statut commande: " + facture.getStatutCommande(), normalFont));
                document.add(commandeInfo);
                document.add(new Paragraph(" ")); // Espace
            }

            // Tableau des produits
            if (facture.getCommande() != null && facture.getCommande().getLignesCommande() != null) {
                PdfPTable productTable = new PdfPTable(5);
                productTable.setWidthPercentage(100);
                productTable.setWidths(new float[]{3, 1, 1, 1, 1});

                // En-têtes du tableau
                String[] headers = {"Produit", "Qté", "Prix Unit.", "TVA", "Total"};
                for (String header : headers) {
                    PdfPCell headerCell = new PdfPCell(new Paragraph(header, boldFont));
                    headerCell.setBackgroundColor(Color.LIGHT_GRAY);
                    headerCell.setPadding(8f);
                    headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    productTable.addCell(headerCell);
                }

                // Lignes des produits
                for (LigneCommande ligne : facture.getCommande().getLignesCommande()) {
                    productTable.addCell(new PdfPCell(new Paragraph(ligne.getProduit().getNom(), normalFont)));
                    productTable.addCell(new PdfPCell(new Paragraph(ligne.getQuantite().toString(), normalFont)));
                    productTable.addCell(new PdfPCell(new Paragraph(ligne.getPrixUnitaire() + " DT", normalFont)));

                    BigDecimal tauxTVA = facture.getCommande().getTauxTVA() != null ? facture.getCommande().getTauxTVA() : BigDecimal.ZERO;
                    productTable.addCell(new PdfPCell(new Paragraph(tauxTVA + "%", normalFont)));
                    productTable.addCell(new PdfPCell(new Paragraph(ligne.getSousTotal() + " DT", normalFont)));
                }

                document.add(productTable);
                document.add(new Paragraph(" ")); // Espace
            }

            // Totaux
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(50);
            totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            totalTable.addCell(new PdfPCell(new Paragraph("Sous-total HT:", boldFont)));
            totalTable.addCell(new PdfPCell(new Paragraph(facture.getMontantHT() + " DT", normalFont)));

            totalTable.addCell(new PdfPCell(new Paragraph("TVA:", boldFont)));
            totalTable.addCell(new PdfPCell(new Paragraph(facture.getMontantTVA() + " DT", normalFont)));

            PdfPCell totalLabelCell = new PdfPCell(new Paragraph("TOTAL TTC:", titleFont));
            totalLabelCell.setBackgroundColor(Color.LIGHT_GRAY);
            totalLabelCell.setPadding(8f);

            PdfPCell totalValueCell = new PdfPCell(new Paragraph(facture.getMontantTTC() + " DT", titleFont));
            totalValueCell.setBackgroundColor(Color.LIGHT_GRAY);
            totalValueCell.setPadding(8f);

            totalTable.addCell(totalLabelCell);
            totalTable.addCell(totalValueCell);

            document.add(totalTable);
            document.add(new Paragraph(" ")); // Espace

            // Informations de paiement
            if (facture.getModePaiement() != null) {
                Paragraph paiementInfo = new Paragraph("Mode de paiement: " + getModePaiementLibelle(facture.getModePaiement()), headerFont);
                document.add(paiementInfo);
            }

            // Pied de page
            document.add(new Paragraph(" "));
            Paragraph footer = new Paragraph("Merci pour votre confiance !", normalFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return baos.toByteArray();

        } catch (DocumentException e) {
            throw new RuntimeException("Erreur lors de la génération du PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Obtenir toutes les factures avec filtres
     */
    @Transactional(readOnly = true)
    public List<FactureDTO> obtenirFacturesAvecFiltres(
            StatutFacture statut,
            LocalDate dateDebut,
            LocalDate dateFin,
            Long clientId) {

        List<Facture> factures;

        if (statut != null && dateDebut != null && dateFin != null && clientId != null) {
            // Tous les filtres
            factures = factureRepository.findByStatutAndDateFactureBetweenAndCommande_Client_Id(
                    statut, dateDebut.atStartOfDay(), dateFin.atTime(23, 59, 59), clientId);
        } else if (statut != null && dateDebut != null && dateFin != null) {
            // Statut + période
            factures = factureRepository.findByStatutAndDateFactureBetween(
                    statut, dateDebut.atStartOfDay(), dateFin.atTime(23, 59, 59));
        } else if (dateDebut != null && dateFin != null) {
            // Période seulement
            factures = factureRepository.findByDateFactureBetween(
                    dateDebut.atStartOfDay(), dateFin.atTime(23, 59, 59));
        } else if (statut != null) {
            // Statut seulement
            factures = factureRepository.findByStatut(statut);
        } else if (clientId != null) {
            // Client seulement
            factures = factureRepository.findByCommande_Client_Id(clientId);
        } else {
            // Toutes les factures
            factures = factureRepository.findAll();
        }

        return factures.stream()
                .map(FactureDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les factures non traitées (EN_ATTENTE)
     */
    @Transactional(readOnly = true)
    public List<FactureDTO> obtenirFacturesNonTraitees() {
        List<Facture> factures = factureRepository.findByStatutOrderByDateCreationAsc(StatutFacture.EN_ATTENTE);
        return factures.stream()
                .map(FactureDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les factures payées du mois
     */
    @Transactional(readOnly = true)
    public List<FactureDTO> obtenirFacturesPayeesDuMois() {
        LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
        LocalDate finMois = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        List<Facture> factures = factureRepository.findByStatutAndDateFactureBetween(
                StatutFacture.PAYEE, debutMois.atStartOfDay(), finMois.atTime(23, 59, 59));

        return factures.stream()
                .map(FactureDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir l'historique des factures d'un client
     */
    @Transactional(readOnly = true)
    public List<FactureDTO> obtenirHistoriqueFacturesClient(Long clientId) {
        List<Facture> factures = factureRepository.findByCommande_Client_IdOrderByDateFactureDesc(clientId);
        return factures.stream()
                .map(FactureDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les statistiques des factures
     */
    @Transactional(readOnly = true)
    public FactureStatistiques obtenirStatistiquesFactures() {
        FactureStatistiques stats = new FactureStatistiques();

        stats.setTotalFactures(factureRepository.count());
        stats.setFacturesEnAttente(factureRepository.countByStatut(StatutFacture.EN_ATTENTE));
        stats.setFacturesPayees(factureRepository.countByStatut(StatutFacture.PAYEE));
        stats.setFacturesAnnulees(factureRepository.countByStatut(StatutFacture.ANNULEE));

        // Chiffre d'affaires du mois
        LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
        LocalDate finMois = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        BigDecimal chiffreAffairesMois = factureRepository.sumMontantTTCByStatutAndDateFactureBetween(
                StatutFacture.PAYEE, debutMois.atStartOfDay(), finMois.atTime(23, 59, 59));
        stats.setChiffreAffairesMois(chiffreAffairesMois != null ? chiffreAffairesMois : BigDecimal.ZERO);

        // Chiffre d'affaires total
        BigDecimal chiffreAffairesTotal = factureRepository.sumMontantTTCByStatut(StatutFacture.PAYEE);
        stats.setChiffreAffairesTotal(chiffreAffairesTotal != null ? chiffreAffairesTotal : BigDecimal.ZERO);

        return stats;
    }

    /**
     * Marquer une facture comme payée
     */
    public FactureDTO marquerFacturePayee(Long factureId, ModePaiement modePaiement) {
        Optional<Facture> factureOpt = factureRepository.findById(factureId);
        if (factureOpt.isEmpty()) {
            throw new RuntimeException("Facture non trouvée avec l'ID : " + factureId);
        }

        Facture facture = factureOpt.get();

        if (facture.getStatut() == StatutFacture.PAYEE) {
            throw new RuntimeException("Cette facture est déjà payée");
        }

        if (facture.getStatut() == StatutFacture.ANNULEE) {
            throw new RuntimeException("Impossible de marquer comme payée une facture annulée");
        }

        facture.setStatut(StatutFacture.PAYEE);
        facture.setDatePaiement(LocalDateTime.now());
        facture.setModePaiement(modePaiement);
        facture.setDateModification(LocalDateTime.now());

        Facture factureSauvegardee = factureRepository.save(facture);
        return FactureDTO.fromEntity(factureSauvegardee);
    }

    /**
     * Obtenir une facture par ID
     */
    @Transactional(readOnly = true)
    public Optional<FactureDTO> obtenirFactureParId(Long factureId) {
        return factureRepository.findById(factureId)
                .map(FactureDTO::fromEntity);
    }

    // Méthodes utilitaires
    private String getStatutLibelle(StatutFacture statut) {
        switch (statut) {
            case EN_ATTENTE: return "En attente";
            case PAYEE: return "Payée";
            case ANNULEE: return "Annulée";
            default: return statut.name();
        }
    }

    private String getModePaiementLibelle(ModePaiement mode) {
        switch (mode) {
            case ESPECES: return "Espèces";
            case CARTE_BANCAIRE: return "Carte bancaire";
            case CHEQUE: return "Chèque";
            case VIREMENT: return "Virement";
            default: return mode.name();
        }
    }

    // Classe pour les statistiques
    public static class FactureStatistiques {
        private Long totalFactures;
        private Long facturesEnAttente;
        private Long facturesPayees;
        private Long facturesAnnulees;
        private BigDecimal chiffreAffairesMois;
        private BigDecimal chiffreAffairesTotal;

        // Getters et Setters
        public Long getTotalFactures() { return totalFactures; }
        public void setTotalFactures(Long totalFactures) { this.totalFactures = totalFactures; }

        public Long getFacturesEnAttente() { return facturesEnAttente; }
        public void setFacturesEnAttente(Long facturesEnAttente) { this.facturesEnAttente = facturesEnAttente; }

        public Long getFacturesPayees() { return facturesPayees; }
        public void setFacturesPayees(Long facturesPayees) { this.facturesPayees = facturesPayees; }

        public Long getFacturesAnnulees() { return facturesAnnulees; }
        public void setFacturesAnnulees(Long facturesAnnulees) { this.facturesAnnulees = facturesAnnulees; }

        public BigDecimal getChiffreAffairesMois() { return chiffreAffairesMois; }
        public void setChiffreAffairesMois(BigDecimal chiffreAffairesMois) { this.chiffreAffairesMois = chiffreAffairesMois; }

        public BigDecimal getChiffreAffairesTotal() { return chiffreAffairesTotal; }
        public void setChiffreAffairesTotal(BigDecimal chiffreAffairesTotal) { this.chiffreAffairesTotal = chiffreAffairesTotal; }
    }
}