import SwiftUI
import shared

@main
struct iOSApp: App {
    init() {
        // Initialize the OCPD Assistant core
        KoinKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

struct ContentView: View {
    @State private var isLoading = true
    @State private var assistant: OCPDAssistantManager?

    var body: some View {
        NavigationView {
            VStack(spacing: 20) {
                if isLoading {
                    ProgressView()
                    Text("Loading OCPD Assistant...")
                        .foregroundColor(.secondary)
                } else {
                    VStack(spacing: 8) {
                        Text("🧠 OCPD Assistant")
                            .font(.largeTitle)
                            .fontWeight(.bold)

                        Text("Your Compassionate Productivity Companion")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                    }
                    .padding(.top, 40)

                    Spacer()

                    // Features card
                    VStack(alignment: .leading, spacing: 12) {
                        Text("✅ Features Available:")
                            .font(.headline)

                        VStack(alignment: .leading, spacing: 4) {
                            FeatureRow(text: "Smart Task Breakdown")
                            FeatureRow(text: "Time-Boxed Scheduling")
                            FeatureRow(text: "'Good Enough' Mode")
                            FeatureRow(text: "Anti-Procrastination Tools")
                            FeatureRow(text: "Behavioral Insights")
                            FeatureRow(text: "Natural Language Processing")
                            FeatureRow(text: "Cross-Platform Sync")
                        }
                    }
                    .padding()
                    .background(Color(.systemGray6))
                    .cornerRadius(12)

                    Spacer()

                    Button(action: {
                        // TODO: Navigate to main app interface
                    }) {
                        Text("Get Started")
                            .font(.headline)
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.accentColor)
                            .cornerRadius(12)
                    }
                }
            }
            .padding()
            .navigationTitle("")
            .navigationBarHidden(true)
        }
        .onAppear {
            loadAssistant()
        }
    }

    private func loadAssistant() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
            assistant = OCPDAssistantManager()
            isLoading = false
        }
    }
}

struct FeatureRow: View {
    let text: String

    var body: some View {
        HStack {
            Text("•")
                .foregroundColor(.accentColor)
            Text(text)
                .font(.body)
            Spacer()
        }
    }
}
