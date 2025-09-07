Of course. I've expanded the README to include a dedicated section that details what each slider does.

Here is the updated `README.md` file.

```markdown
# Embizzolator

**Synergize Your Communications Paradigm.**

Embizzolator is a paradigm-shifting, next-generation mobile application designed for the modern business professional. Built on Android with Jetpack Compose, this tool empowers users to leverage cutting-edge AI to translate standard English phrases into high-impact, jargon-heavy corporate speak. Whether you're preparing for a critical stakeholder meeting, drafting a memo, or simply wish to sound more aligned with executive thought leadership, Embizzolator is your go-to solution for enhancing professional communication.

The app interfaces directly with a powerful Large Language Model (LLM), providing real-time, dynamic translations based on a suite of customizable parameters. From jargon density to corporate personas, users have granular control over the output, ensuring every message is perfectly tailored to its intended audience. For security, all sensitive API credentials are encrypted on-device using the Jetpack Security library.

## Key Features

* **AI-Powered Translation:** Utilizes a powerful LLM to dynamically generate corporate jargon.
* **Flexible Input Methods:** Supports both standard keyboard typing and hands-free voice-to-text dictation.
* **Audio Playback:** Listen to the generated jargon with integrated text-to-speech functionality, complete with play and stop controls.
* **Granular Prompt Control:** Fine-tune the AI's output using intuitive sliders for **Jargon Density**, **Urgency Meter**, and **Verbosity**.
* **Dynamic Personas:** Adopt different corporate communication styles, including "Business Executive," "Engineering Manager," "Agile Product Owner," and "Marketing Executive."
* **Customizable Theming:** Instantly change the app's entire look and feel by selecting different "Branding Guidelines," from the professional "Executive Mahogany" to the whimsical "Marketing" theme.
* **Secure Credential Storage:** API keys and endpoints are protected on-device using `EncryptedSharedPreferences`, with an optional password lock for an added layer of security.

## How to Use

Using Embizzolator is a seamless, three-step process designed for maximum efficiency.

### 1. First-Time Setup
Before you can synergize, you must configure the application's backend.
1.  Navigate to the **TPS Configuration** screen using the bottom navigation bar.
2.  Enter your **API Endpoint URL**, **API Key**, and the specific **Model Name** you wish to use.
3.  (Optional) Click the **"Lock"** button to set a password that will protect your credentials from being viewed or edited without authorization.
4.  Click **"Save"**. The app will securely store these settings. An error banner will be displayed on the main screen until this step is complete.

### 2. Generate Jargon
1.  On the **KPI Dashboard** screen, enter a phrase into the "Core Competency Input" field by typing or by tapping the microphone icon for voice dictation.
2.  Click the **"Synergize!"** button.
3.  The translated, jargon-heavy response will appear in the "Synergistic Output" field below.

### 3. Review and Playback
1.  Read the generated output in the text field. If the response is long, the field is scrollable.
2.  Use the icons to interact with the output:
    * **Play Icon (▶️):** Reads the response aloud.
    * **Stop Icon (⏹️):** Stops the playback.
    * **Clear Icon (X):** Clears the output field, resetting it to the default text.

## Customizing Your Output

The **TPS Configuration** screen gives you deep control over the LLM's output.

### Prompt Parameters (Sliders)
Each slider is divided into five increments (Low, Medium-Low, Medium, Medium-High, High) to precisely tune the AI's response.

* **Jargon Density:** This slider controls the concentration of corporate buzzwords, acronyms, and complex terminology in the final output. A "Low" setting will produce a professional but relatively clear response, while a "High" setting will generate text that is saturated with the most opaque and impressive-sounding jargon imaginable.

* **Urgency Meter:** This adjusts the tone of the response to sound more or less time-sensitive. A "Low" setting will result in calm, strategic language, while a "High" setting will infuse the text with a palpable sense of immediacy, action items, and critical deadlines.

* **Verbosity:** This determines the overall length and level of detail in the generated text. A "Low" setting provides a concise, to-the-point statement. A "High" setting will produce a much longer, more elaborate response that explores the topic from multiple synergistic angles, perfect for filling a presentation slide.

### Corporate Style (Persona)
This dropdown allows you to command the AI to adopt the voice of a specific corporate role, influencing the type of jargon it uses.

### Branding Guidelines (Theming)
This dropdown changes the entire color scheme of the application to match different corporate aesthetics, from the classic "Executive Mahogany" to the chaotic "Marketing" theme.
```