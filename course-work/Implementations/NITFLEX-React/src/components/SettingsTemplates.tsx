import { Header } from "./Header"
import { Navbar } from "./Navbar"
import './SettingsTemplates.css'

export const SettingsPageTemplate = (props: {title: string, additionalInfo?: string, children: React.ReactElement | React.ReactElement[]}) => {
    return (
        <div className="Settings-page vertical">
            <Header />
            <div className="Navbar-content horizontal">
                <div className="Navbar-container">
                    <Navbar />
                </div>
                <div className="Content vertical">
                    <div className="Settings-Heading horizontal">
                        <h1 className="Title">{props.title + (props.additionalInfo ? ` [${props.additionalInfo}]` : '')}</h1>
                    </div>
                    <hr/>
                    {props.children}
                </div>
            </div>
        </div>
    )
}

export const SettingSection = (props: {title: string, separatorLine?: boolean, children: React.ReactElement | React.ReactElement[]}) => {
    return (
        <div className="Setting-section vertical">
            <h2 className="Title">{props.title}</h2>
            {props.separatorLine && <hr/>}
            {props.children}
        </div>
    )
}

export const HorizontalSetting = (props: {label: string, children: React.ReactElement | React.ReactElement[]}) => {
    return (
        <div className="Horizontal-setting horizontal">
            <p>{props.label}</p>
            {props.children}
        </div>
    )
}
