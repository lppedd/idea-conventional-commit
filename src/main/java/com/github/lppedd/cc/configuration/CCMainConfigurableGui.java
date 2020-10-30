package com.github.lppedd.cc.configuration;

import static com.intellij.uiDesigner.core.GridConstraints.*;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.lppedd.cc.CCBundle;
import com.github.lppedd.cc.configuration.CCConfigService.CompletionType;
import com.github.lppedd.cc.configuration.CCDefaultTokensService.JsonCommitType;
import com.github.lppedd.cc.configuration.component.DefaultTokensPanel;
import com.github.lppedd.cc.configuration.component.DefaultTokensFilePickerPanel;
import com.github.lppedd.cc.configuration.holders.DefaultsFileExportHolder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.labels.ActionLink;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;

/**
 * @author Edoardo Luppi
 */
public class CCMainConfigurableGui {
  private JPanel rootPanel;

  private JPanel infoPanel;
  private JBLabel info;

  private final ButtonGroup group = new ButtonGroup();
  private JBRadioButton isPopup;
  private JBRadioButton isTemplate;

  private JPanel defaultsPanel;
  private DefaultTokensFilePickerPanel defaultTokensFilePickerPanel;
  private final DefaultTokensPanel defaultTokensPanel = new DefaultTokensPanel();

  public CCMainConfigurableGui(
      @NotNull final Project project,
      @NotNull final Disposable disposable) {
    this();
    finishUpComponents(project, disposable);
  }

  private CCMainConfigurableGui() {}

  @NotNull
  public JPanel getRootPanel() {
    return rootPanel;
  }

  @NotNull
  public CompletionType getCompletionType() {
    if (isPopup.isSelected()) {
      return CompletionType.POPUP;
    }

    if (isTemplate.isSelected()) {
      return CompletionType.TEMPLATE;
    }

    throw new IllegalStateException("A radio button should be selected");
  }

  @Nullable
  public String getCustomFilePath() {
    return defaultTokensFilePickerPanel.getCustomFilePath();
  }

  public void setCompletionType(@NotNull final CompletionType completionType) {
    switch (completionType) {
      case POPUP:
        isPopup.setSelected(true);
        break;
      case TEMPLATE:
        isTemplate.setSelected(true);
        break;
      default:
        break;
    }
  }

  public void setCustomFilePath(@Nullable final String path) {
    defaultTokensFilePickerPanel.setCustomFilePath(path);
  }

  public void setTokens(@NotNull final Map<String, JsonCommitType> tokens) {
    defaultTokensPanel.setTokens(tokens);
  }

  public boolean isValid() {
    return defaultTokensFilePickerPanel.isComponentValid();
  }

  public void revalidate() {
    defaultTokensFilePickerPanel.revalidateComponent();
  }

  private void finishUpComponents(
      @NotNull final Project project,
      @NotNull final Disposable disposable) {
    infoPanel.add(Box.createHorizontalStrut(10));
    infoPanel.add(new ActionLink(CCBundle.get("cc.config.info.learnMore"), new LearnMoreAction()));

    group.add(isPopup);
    group.add(isTemplate);

    info.setText(CCBundle.get("cc.config.info"));
    isPopup.setText(CCBundle.get("cc.config.popup"));
    isTemplate.setText(CCBundle.get("cc.config.template"));

    defaultsPanel.setLayout(new GridLayoutManager(3, 1));
    defaultsPanel.setBorder(
        IdeBorderFactory.createTitledBorder(
            CCBundle.get("cc.config.defaults"),
            false,
            JBUI.insetsTop(7)
        )
    );

    final GridConstraints gc = new GridConstraints();
    gc.setIndent(1);
    gc.setFill(FILL_BOTH);

    // noinspection ConstantExpression
    gc.setHSizePolicy(SIZEPOLICY_CAN_SHRINK | SIZEPOLICY_CAN_GROW | SIZEPOLICY_WANT_GROW);
    defaultsPanel.add(new DefaultsFileExportHolder().getComponent(), gc);

    gc.setRow(1);
    defaultTokensFilePickerPanel = new DefaultTokensFilePickerPanel(project, disposable);
    defaultsPanel.add(defaultTokensFilePickerPanel, gc);
    defaultTokensFilePickerPanel.revalidateComponent();

    gc.setRow(2);
    gc.setIndent(0);

    // noinspection ConstantExpression
    gc.setVSizePolicy(SIZEPOLICY_CAN_SHRINK | SIZEPOLICY_CAN_GROW | SIZEPOLICY_WANT_GROW);
    defaultsPanel.add(defaultTokensPanel, gc);
  }

  private static class LearnMoreAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
      try {
        Desktop.getDesktop().browse(new URI(CCBundle.get("cc.plugin.repository")));
      } catch (final IOException | URISyntaxException ignored) {
        // Ignored for now
      }
    }
  }
}
