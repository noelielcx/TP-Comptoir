package comptoirs.service;

import comptoirs.dao.CommandeRepository;
import comptoirs.dao.LigneRepository;
import comptoirs.dao.ProduitRepository;
import comptoirs.entity.Ligne;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;

@Service
@Validated // Les contraintes de validatipn des méthodes sont vérifiées
public class LigneService {
    // La couche "Service" utilise la couche "Accès aux données" pour effectuer les traitements
    private final CommandeRepository commandeDao;
    private final LigneRepository ligneDao;
    private final ProduitRepository produitDao;

    // @Autowired
    // La couche "Service" utilise la couche "Accès aux données" pour effectuer les traitements
    public LigneService(CommandeRepository commandeDao, LigneRepository ligneDao, ProduitRepository produitDao) {
        this.commandeDao = commandeDao;
        this.ligneDao = ligneDao;
        this.produitDao = produitDao;
    }

    /**
     * <pre>
     * Service métier : 
     *     Enregistre une nouvelle ligne de commande pour une commande connue par sa clé,
     *     Incrémente la quantité totale commandée (Produit.unitesCommandees) avec la quantite à commander
     * Règles métier :
     *     - le produit référencé doit exister
     *     - la commande doit exister
     *     - la commande ne doit pas être déjà envoyée (le champ 'envoyeele' doit être null)
     *     - la quantité doit être positive
     *     - On doit avoir une quantite en stock du produit suffisante
     * <pre>
     * 
     *  @param commandeNum la clé de la commande
     *  @param produitRef la clé du produit
     *  @param quantite la quantité commandée (positive)
     *  @return la ligne de commande créée
     */
    @Transactional
    Ligne ajouterLigne(Integer commandeNum, Integer produitRef, @Positive int quantite) {
        //throw new UnsupportedOperationException("Cette méthode n'est pas implémentée");
        // vérifications
        var produit = produitDao.findById(produitRef).orElseThrow();
        var commande = commandeDao.findById(commandeNum).orElseThrow();
        if (commande.getEnvoyeele() == null) { //la commande n'a pas été envoyée
            commande.setEnvoyeele(LocalDate.now());
        } else{
            throw new UnsupportedOperationException("La commande a déjà été expédiée");
        }
        if (quantite < 0){
            throw new UnsupportedOperationException("La quantité doit > 0");
        } else{
            if (quantite > produit.getUnitesEnStock()){
                throw new UnsupportedOperationException("La quantité ne peut pas dépasser le stock disponiible");
            } else{ // si tout va bien
                produit.setUnitesEnStock((produit.getUnitesEnStock())- quantite);
            } // création de la ligne
            var newLigne = new Ligne(commande, produit, quantite);
            produit.setUnitesCommandees(produit.getUnitesCommandees() + newLigne.getQuantite());
            // ajouter à la quantité totale commandée, la quantité à commander
            commande.getLignes().add(newLigne);

            return newLigne;
        }
    }
}

