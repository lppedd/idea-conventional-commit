package com.github.lppedd.cc.configuration;

import static com.intellij.uiDesigner.core.GridConstraints.*;

import java.util.Map;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.lppedd.cc.CCBundle;
import com.github.lppedd.cc.configuration.CCConfigService.CompletionType;
import com.github.lppedd.cc.configuration.CCDefaultTokensService.JsonCommitType;
import com.github.lppedd.cc.configuration.component.DefaultTokensFileExportPanel;
import com.github.lppedd.cc.configuration.component.DefaultTokensFilePickerPanel;
import com.github.lppedd.cc.configuration.component.DefaultTokensPanel;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;

/**
 * @author Edoardo Luppi
 */
public class CCMainConfigurableGui {
  private static final int INDENT = 10;

  private JPanel rootPanel;
  private JPanel infoPanel;
  private JBLabel info;

  private JPanel completionTypePanel;
  private final ButtonGroup group = new ButtonGroup();
  private final JBRadioButton isPopup = new JBRadioButton(CCBundle.get("cc.config.popup"));
  private final JBRadioButton isTemplate = new JBRadioButton(CCBundle.get("cc.config.template"));

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

  @SuppressWarnings("ConstantExpression")
  private void finishUpComponents(
      @NotNull final Project project,
      @NotNull final Disposable disposable) {
    completionTypePanel.setLayout(
        new GridLayoutManager(2, 1, JBUI.insetsLeft(INDENT), 0, JBUI.scale(5))
    );

    final GridConstraints gcCtp = new GridConstraints();
    gcCtp.setFill(FILL_HORIZONTAL);
    completionTypePanel.add(isPopup, gcCtp);

    gcCtp.setRow(1);
    completionTypePanel.add(isTemplate, gcCtp);

    group.add(isPopup);
    group.add(isTemplate);

    info.setText(CCBundle.get("cc.config.info"));

    defaultsPanel.setLayout(new GridLayoutManager(3, 1, JBUI.insetsLeft(INDENT), 0, 0));
    defaultsPanel.setBorder(
        IdeBorderFactory.createTitledBorder(
            CCBundle.get("cc.config.defaults"),
            false,
            JBUI.insetsTop(3)
        )
    );

    final GridConstraints gc = new GridConstraints();
    gc.setFill(FILL_BOTH);
    gc.setHSizePolicy(SIZEPOLICY_CAN_SHRINK | SIZEPOLICY_CAN_GROW | SIZEPOLICY_WANT_GROW);
    defaultsPanel.add(JBUI.Borders.empty(0, 1, 16, 0).wrap(new DefaultTokensFileExportPanel()), gc);

    gc.setRow(1);
    defaultTokensFilePickerPanel = new DefaultTokensFilePickerPanel(project, disposable);
    defaultsPanel.add(defaultTokensFilePickerPanel, gc);
    defaultTokensFilePickerPanel.revalidateComponent();

    gc.setRow(2);
    gc.setVSizePolicy(SIZEPOLICY_CAN_SHRINK | SIZEPOLICY_CAN_GROW | SIZEPOLICY_WANT_GROW);
    defaultsPanel.add(defaultTokensPanel, gc);
  }
}
