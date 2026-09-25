import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    @State private var detailActive = false

    var body: some View {
        ComposeView()
            .ignoresSafeArea()
            .preferredColorScheme(detailActive ? .dark : nil)
            .onReceive(NotificationCenter.default.publisher(for: Notification.Name("PhotoDetailDidOpen"))) { _ in
                detailActive = true
            }
            .onReceive(NotificationCenter.default.publisher(for: Notification.Name("PhotoDetailDidClose"))) { _ in
                detailActive = false
            }
    }
}
