/*
 * The Initial Developer of the content of this file is NETCONOMY.
 * All portions of the code written by NETCONOMY are property of NETCONOMY. All Rights Reserved.
 *
 * NETCONOMY Software & Consulting GmbH
 * Hilmgasse 4, A-8010 Graz (Austria)
 * FN 204360 f, Landesgericht fuer ZRS Graz
 * Tel: +43 (316) 815 544
 * Fax: +43 (316) 815544-99
 * www.netconomy.net
 *
 * (c) 2016 by NETCONOMY Software & Consulting GmbH
 */

package net.netconomy.abrakadabra.facades.process.email.context;/**
 * Created by user on 9/21/2016.
 */

import net.netconomy.abrakadabra.core.model.process.FollowUpEmailProcessModel;
import net.netconomy.abrakadabra.facades.order.CustomerOrderEntryStatus;
import net.netconomy.internationalization.service.LocalizationEntryService;

import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commercefacades.order.data.ConsignmentData;
import de.hybris.platform.commercefacades.order.data.ConsignmentEntryData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author Mahir Zukic on 9/21/2016.
 */
public class FollowUpEmailContext extends AbstractAbrakadabraEmailContext<FollowUpEmailProcessModel> {

    private OrderModel orderModel;

    private OrderData orderData;

    private UserModel userModel;

    private Map<String, String> neededLocalizations;

//    private LocalizationEntryService defaultLocalizationEntryService;

    private ConfigurationService configurationService;

    private Converter<OrderModel, OrderData> orderConverter;

    private final static String EMAIL_FROM_ADDRESS = "email.followup.fromaddress";

    private final static String EMAIL_DISPLAY_NAME = "email.followup.displayname";

    @Override
    public void init(FollowUpEmailProcessModel businessProcessModel, EmailPageModel emailPageModel) {
        super.init(businessProcessModel, emailPageModel);
        orderModel = businessProcessModel.getOrder();
        userModel = (UserModel) businessProcessModel.getCustomer();
        CustomerModel customerModel = (CustomerModel) userModel;
        neededLocalizations = new HashMap();

        if (orderModel != null) {
            orderData = orderConverter.convert(orderModel);
        }
        if (orderData == null) {
            return;
        }

        // find a list of consignments which are delivered to the customer
        // and match this list with the list of order entries
        List<ConsignmentData> deliveredConsignments = findConsignments(orderData, CustomerOrderEntryStatus.DELIVERED);
        orderData.setConsignments(deliveredConsignments);
        List<OrderEntryData> filteredEntries = new ArrayList<>();
        for (OrderEntryData orderEntry : orderData.getEntries()) {
            for (ConsignmentData consData : deliveredConsignments) {
                for (ConsignmentEntryData consEntry : consData.getEntries()) {
                    if (consEntry.getOrderEntry().getProduct().getCode().equals(orderEntry.getProduct().getCode())) {
                        filteredEntries.add(orderEntry);
                        break;
                    }
                }
            }
        }

        // set only entries whose consigments are delivered to the customer
        // this will be only used for displaying products for review
        orderData.setEntries(filteredEntries);

        Locale currentLocale = new Locale(getBaseSite().getLocale().substring(0, 2));

        // TODO: fix this with real value of main logo image, top left of homepage - yellow star
        final String mainImageUrl = getSecureBaseUrl() + "/_ui/desktop/theme-blue/images/email-logo.png";

//        final String aboveSalutation =
//                defaultLocalizationEntryService.getLocalizationEntry("email.followup.text.above.table.salutation")
//                        .getTranslation(currentLocale);
//        final String mainText =
//                defaultLocalizationEntryService.getLocalizationEntry("email.followup.text.above.table.maintext")
//                        .getTranslation(currentLocale);
//        final String belowSalutation =
//                defaultLocalizationEntryService.getLocalizationEntry("email.followup.text.below.table.salutation")
//                        .getTranslation(currentLocale);
//        final String opinion =
//                defaultLocalizationEntryService.getLocalizationEntry("email.followup.text.above.table.opinion")
//                        .getTranslation(currentLocale);
//        final String review = defaultLocalizationEntryService.getLocalizationEntry("email.followup.button.text")
//                .getTranslation(currentLocale);
//        final String subject = defaultLocalizationEntryService.getLocalizationEntry("email.followup.subject")
//                .getTranslation(currentLocale);


        put("mainImageUrl", mainImageUrl);
//        neededLocalizations.put("mainImageUrl", mainImageUrl);
//        neededLocalizations.put("aboveSalutation", aboveSalutation);
//        neededLocalizations.put("mainText", mainText);
//        neededLocalizations.put("belowSalutation", belowSalutation);
//        neededLocalizations.put("opinion", opinion);
//        neededLocalizations.put("review", review);
//        neededLocalizations.put("subject", subject);

        put("orderData", orderData);



        put(DISPLAY_NAME, userModel.getDisplayName());
        put(FROM_DISPLAY_NAME, configurationService.getConfiguration().getString(EMAIL_DISPLAY_NAME));
        put(EMAIL, customerModel.getContactEmail());
        put(FROM_EMAIL,configurationService.getConfiguration().getString(EMAIL_FROM_ADDRESS) );
    }

    private List<ConsignmentData> findConsignments(OrderData target, CustomerOrderEntryStatus delivered) {
        return target.getConsignments().stream()
                .filter(consignmentData ->
                        consignmentData.getCustomerDisplayStatus()
                                == delivered
                ).collect(Collectors.toList());
    }

    @Override
    protected BaseSiteModel getSite(FollowUpEmailProcessModel businessProcessModel) {
        return businessProcessModel.getOrder().getSite();
    }

    @Override
    protected CustomerModel getCustomer(FollowUpEmailProcessModel businessProcessModel) {
        return (CustomerModel) businessProcessModel.getOrder().getUser();
    }

    @Override
    protected LanguageModel getEmailLanguage(FollowUpEmailProcessModel businessProcessModel) {
        return businessProcessModel.getOrder().getLanguage();
    }

    public OrderModel getOrderModel() {
        return orderModel;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public Map<String, String> getNeededLocalizations() {
        return neededLocalizations;
    }

    public Converter<OrderModel, OrderData> getOrderConverter() {
        return orderConverter;
    }

    public void setOrderConverter(
            Converter<OrderModel, OrderData> orderConverter) {
        this.orderConverter = orderConverter;
    }

//    public LocalizationEntryService getDefaultLocalizationEntryService() {
//        return defaultLocalizationEntryService;
//    }
//
//    public void setDefaultLocalizationEntryService(
//            LocalizationEntryService defaultLocalizationEntryService) {
//        this.defaultLocalizationEntryService = defaultLocalizationEntryService;
//    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
}
