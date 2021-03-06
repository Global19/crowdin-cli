package com.crowdin.cli.commands.actions;

import com.crowdin.cli.client.Client;
import com.crowdin.cli.client.CrowdinProject;
import com.crowdin.cli.commands.ClientAction;
import com.crowdin.cli.commands.Outputter;
import com.crowdin.cli.properties.PropertiesBean;
import com.crowdin.cli.utils.console.ConsoleSpinner;
import com.crowdin.client.translationstatus.model.LanguageProgress;

import java.util.List;

import static com.crowdin.cli.BaseCli.RESOURCE_BUNDLE;

class StatusAction implements ClientAction {

    private boolean noProgress;
    private String languageId;
    private boolean isVerbose;
    private boolean showTranslated;
    private boolean showApproved;

    public StatusAction(boolean noProgress, String languageId, boolean isVerbose, boolean showTranslated, boolean showApproved) {
        this.noProgress = noProgress;
        this.languageId = languageId;
        this.isVerbose = isVerbose;
        this.showTranslated = showTranslated;
        this.showApproved = showApproved;
    }

    @Override
    public void act(Outputter out, PropertiesBean pb, Client client) {
        CrowdinProject project = ConsoleSpinner.execute(out, "message.spinner.fetching_project_info", "error.collect_project_info",
            this.noProgress, false, client::downloadProjectWithLanguages);

        if (languageId != null) {
            project.findLanguageById(languageId, true)
                .orElseThrow(() -> new RuntimeException(String.format(RESOURCE_BUNDLE.getString("error.not_found_language"), languageId)));
        }

        List<LanguageProgress> progresses = client.getProjectProgress(languageId);

        if (isVerbose) {
            progresses.forEach(pr -> {
                out.println(String.format(RESOURCE_BUNDLE.getString("message.language"),
                    project.findLanguageById(pr.getLanguageId(), true).get().getName(), pr.getLanguageId()));
                if (showTranslated) {
                    out.println(String.format(RESOURCE_BUNDLE.getString("message.translation_progress"),
                        pr.getTranslationProgress(),
                        pr.getWords().getTranslated(), pr.getWords().getTotal(),
                        pr.getPhrases().getTranslated(), pr.getPhrases().getTotal()));
                }
                if (showApproved) {
                    out.println(String.format(RESOURCE_BUNDLE.getString("message.approval_progress"),
                        pr.getApprovalProgress(),
                        pr.getWords().getApproved(), pr.getWords().getTotal(),
                        pr.getPhrases().getApproved(), pr.getPhrases().getTotal()));
                }
            });
        } else {
            if (showTranslated && showApproved) {
                out.println(RESOURCE_BUNDLE.getString("message.translation"));
            }
            if (showTranslated) {
                progresses.forEach(pr -> out.println(String.format(RESOURCE_BUNDLE.getString("message.item_list_with_percents"),
                    pr.getLanguageId(), pr.getTranslationProgress())));
            }
            if (showTranslated && showApproved) {
                out.println(RESOURCE_BUNDLE.getString("message.approval"));
            }
            if (showApproved) {
                progresses.forEach(pr -> out.println(String.format(RESOURCE_BUNDLE.getString("message.item_list_with_percents"),
                    pr.getLanguageId(), pr.getApprovalProgress())));
            }
        }
    }
}
