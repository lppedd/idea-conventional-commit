package com.github.lppedd.cc.configuration;

import static com.intellij.uiDesigner.core.GridConstraints.*;

import java.awt.*;
import java.awt.event.ActionEvent;
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
import com.github.lppedd.cc.configuration.holders.DefaultsFileExportHolder;
import com.github.lppedd.cc.configuration.holders.DefaultsFilePickerHolder;
import com.github.lppedd.cc.configuration.holders.DefaultsListsHolder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.labels.SwingActionLink;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

/**
 * @author Edoardo Luppi
 */
public class CCMainConfigurableGui {
  private JPanel rootPanel;

  private JPanel infoPanel;
  private JBLabel info;

  private final ButtonGroup group = new ButtonGroup();
  private JBRadioButton isAutoPopup;
  private JBRadioButton isTemplate;

  private JPanel defaultsPanel;
  private DefaultsFilePickerHolder defaultsFilePickerHolder;
  private final DefaultsListsHolder defaultsListsHolder = new DefaultsListsHolder();

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
    if (isAutoPopup.isSelected()) {
      return CompletionType.AUTOPOPUP;
    }

    if (isTemplate.isSelected()) {
      return CompletionType.TEMPLATE;
    }

    throw new IllegalStateException("A radio button should be selected");
  }

  @Nullable
  public String getCustomFilePath() {
    return defaultsFilePickerHolder.getCustomFilePath();
  }

  public void setCompletionType(@NotNull final CompletionType completionType) {
    switch (completionType) {
      case AUTOPOPUP:
        isAutoPopup.setSelected(true);
        break;
      case TEMPLATE:
        isTemplate.setSelected(true);
        break;
      default:
        break;
    }
  }

  public void setCustomFilePath(@Nullable final String path) {
    defaultsFilePickerHolder.setCustomFilePath(path);
  }

  public void setTokens(@NotNull final Map<String, JsonCommitType> tokens) {
    defaultsListsHolder.setTokens(tokens);
  }

  public boolean isValid() {
    return defaultsFilePickerHolder.isValid();
  }

  public void revalidate() {
    defaultsFilePickerHolder.revalidate();
  }

  private void finishUpComponents(
      @NotNull final Project project,
      @NotNull final Disposable disposable) {
    infoPanel.add(Box.createHorizontalStrut(10));
    infoPanel.add(new SwingActionLink(new LearnMoreAction()));

    group.add(isAutoPopup);
    group.add(isTemplate);

    info.setText(CCBundle.get("cc.config.info"));
    isAutoPopup.setText(CCBundle.get("cc.config.autoPopup"));
    isTemplate.setText(CCBundle.get("cc.config.template"));

    defaultsPanel.setLayout(new GridLayoutManager(3, 1));
    defaultsPanel.setBorder(
        IdeBorderFactory.createTitledBorder(
            CCBundle.get("cc.config.defaults"),
            false
        )
    );

    final GridConstraints gc = new GridConstraints();
    gc.setIndent(1);
    gc.setFill(FILL_BOTH);
    gc.setHSizePolicy(SIZEPOLICY_CAN_SHRINK | SIZEPOLICY_CAN_GROW | SIZEPOLICY_WANT_GROW);
    defaultsPanel.add(new DefaultsFileExportHolder().getComponent(), gc);

    gc.setRow(1);
    defaultsFilePickerHolder = new DefaultsFilePickerHolder(project, disposable);
    defaultsPanel.add(defaultsFilePickerHolder.getComponent(), gc);
    defaultsFilePickerHolder.revalidate();

    gc.setRow(2);
    gc.setIndent(0);
    gc.setVSizePolicy(SIZEPOLICY_CAN_SHRINK | SIZEPOLICY_CAN_GROW | SIZEPOLICY_WANT_GROW);
    defaultsPanel.add(defaultsListsHolder.getComponent(), gc);
  }

  private static class LearnMoreAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    LearnMoreAction() {
      super(CCBundle.get("cc.config.info.learnMore"));
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      try {
        Desktop.getDesktop().browse(new URI(CCBundle.get("cc.plugin.repository")));
      } catch (final IOException | URISyntaxException ignored) {
        // Ignored for now
      }
    }
  }
}
